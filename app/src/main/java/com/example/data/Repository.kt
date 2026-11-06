package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SamRepository(val dao: SamDao) {

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

    suspend fun logFollowup(followup: LongitudinalFollowup) {
        withContext(Dispatchers.IO) {
            dao.insertFollowup(followup)
        }
    }

    fun getAllChildren() = dao.getAllChildren()

    suspend fun getAllHouseholds(): List<Household> = withContext(Dispatchers.IO) { dao.getAllHouseholds() }
    suspend fun getAllChildrenList(): List<Child> = withContext(Dispatchers.IO) { dao.getAllChildrenList() }
    suspend fun getAllBaselines(): List<BaselineAssessment> = withContext(Dispatchers.IO) { dao.getAllBaselines() }
    suspend fun getAllFollowups(): List<LongitudinalFollowup> = withContext(Dispatchers.IO) { dao.getAllFollowups() }
    suspend fun deleteChildRecord(studyId: String) { withContext(Dispatchers.IO) { dao.deleteChildRecord(studyId) } }
    suspend fun insertHousehold(hh: Household) = withContext(Dispatchers.IO) { dao.insertHousehold(hh) }
    suspend fun insertChild(ch: Child) = withContext(Dispatchers.IO) { dao.insertChild(ch) }
    suspend fun insertBaseline(b: BaselineAssessment) = withContext(Dispatchers.IO) { dao.insertBaseline(b) }

}
