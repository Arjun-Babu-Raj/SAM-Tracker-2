package com.example.data

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SamRepository(val dao: SamDao, private val db: AppDatabase? = null) {

    suspend fun <R> runInTransaction(block: suspend () -> R): R = withContext(Dispatchers.IO) {
        if (db != null) {
            db.withTransaction { block() }
        } else {
            block()
        }
    }

    suspend fun logBaseline(household: Household, child: Child, baseline: BaselineAssessment) {
        withContext(Dispatchers.IO) {
            dao.logBaselineTransaction(household, child, baseline)
        }
    }

    suspend fun searchChild(query: String): Child? {
        return withContext(Dispatchers.IO) {
            dao.getChildByStudyOrCsamId(query)
        }
    }

    suspend fun searchChildExact(query: String): Child? {
        return withContext(Dispatchers.IO) {
            dao.getChildByExactStudyOrCsamId(query) ?: dao.getChildByExactName(query)
        }
    }

    suspend fun getChildByStudyId(studyId: String): Child? = withContext(Dispatchers.IO) {
        dao.getChildByStudyId(studyId)
    }

    suspend fun getChildByCsamId(csamId: String): Child? = withContext(Dispatchers.IO) {
        dao.getChildByCsamId(csamId)
    }

    suspend fun getFollowupByStudyIdAndRound(studyId: String, round: String): LongitudinalFollowup? = withContext(Dispatchers.IO) {
        dao.getFollowupByStudyIdAndRound(studyId, round)
    }

    suspend fun logFollowup(followup: LongitudinalFollowup) {
        withContext(Dispatchers.IO) {
            val existing = dao.getFollowupByStudyIdAndRound(followup.studyId, followup.followupRound)
            val followupToSave = if (existing != null) {
                followup.copy(followupId = existing.followupId)
            } else {
                followup
            }
            dao.insertFollowup(followupToSave)
        }
    }

    fun getAllChildren() = dao.getAllChildren()
    fun getAllFollowupsFlow() = dao.getAllFollowupsFlow()
    fun getAllBaselinesFlow() = dao.getAllBaselinesFlow()

    suspend fun getAllHouseholds(): List<Household> = withContext(Dispatchers.IO) { dao.getAllHouseholds() }
    suspend fun getAllChildrenList(): List<Child> = withContext(Dispatchers.IO) { dao.getAllChildrenList() }
    suspend fun getAllBaselines(): List<BaselineAssessment> = withContext(Dispatchers.IO) { dao.getAllBaselines() }
    suspend fun getAllFollowups(): List<LongitudinalFollowup> = withContext(Dispatchers.IO) { dao.getAllFollowups() }
    suspend fun deleteChildRecord(studyId: String) { withContext(Dispatchers.IO) { dao.deleteChildRecord(studyId) } }
    suspend fun insertHousehold(hh: Household) = withContext(Dispatchers.IO) { dao.insertHousehold(hh) }
    suspend fun insertChild(ch: Child) = withContext(Dispatchers.IO) { dao.insertChild(ch) }
    suspend fun insertBaseline(b: BaselineAssessment) = withContext(Dispatchers.IO) { dao.insertBaseline(b) }

}
