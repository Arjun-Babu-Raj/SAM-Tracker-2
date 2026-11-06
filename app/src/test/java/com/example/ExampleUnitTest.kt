package com.example

import com.example.data.*
import com.example.ui.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.StringReader

class FakeSamDao : SamDao {
    private val householdsState = MutableStateFlow<Map<String, Household>>(emptyMap())
    private val childrenState = MutableStateFlow<Map<String, Child>>(emptyMap())
    private val baselinesState = MutableStateFlow<Map<String, BaselineAssessment>>(emptyMap())
    private val followupsState = MutableStateFlow<List<LongitudinalFollowup>>(emptyList())
    private val draftsState = MutableStateFlow<Map<String, Draft>>(emptyMap())
    private var nextFollowupId = 1

    fun createTransactionWrapper(): suspend (suspend () -> Unit) -> Unit = { block ->
        val hhSnap = householdsState.value
        val chSnap = childrenState.value
        val bSnap = baselinesState.value
        val fSnap = followupsState.value
        val dSnap = draftsState.value
        val idSnap = nextFollowupId
        try {
            block()
        } catch (t: Throwable) {
            householdsState.value = hhSnap
            childrenState.value = chSnap
            baselinesState.value = bSnap
            followupsState.value = fSnap
            draftsState.value = dSnap
            nextFollowupId = idSnap
            throw t
        }
    }

    override suspend fun insertHousehold(household: Household) {
        householdsState.value = householdsState.value.toMutableMap().apply {
            put(household.householdId, household)
        }
    }

    override suspend fun insertChild(child: Child) {
        val existingCsam = childrenState.value.values.find {
            it.csamId == child.csamId && it.studyId != child.studyId
        }
        if (existingCsam != null) {
            throw android.database.sqlite.SQLiteConstraintException("UNIQUE constraint failed: children.csamId")
        }
        childrenState.value = childrenState.value.toMutableMap().apply {
            put(child.studyId, child)
        }
    }

    override suspend fun getChildByStudyId(studyId: String): Child? = childrenState.value[studyId]

    override suspend fun getChildByCsamId(csamId: String): Child? =
        childrenState.value.values.find { it.csamId == csamId }

    override suspend fun insertBaseline(baseline: BaselineAssessment) {
        baselinesState.value = baselinesState.value.toMutableMap().apply {
            put(baseline.studyId, baseline)
        }
    }

    override suspend fun getChildByExactStudyOrCsamId(query: String): Child? =
        childrenState.value.values.find { it.studyId == query || it.csamId == query }

    override suspend fun getChildByExactName(query: String): Child? =
        childrenState.value.values.find { it.childName.equals(query, ignoreCase = true) }

    override suspend fun getChildByStudyOrCsamId(query: String): Child? =
        getChildByExactStudyOrCsamId(query)
            ?: getChildByExactName(query)
            ?: childrenState.value.values.find { it.childName.contains(query, ignoreCase = true) }

    override fun getAllChildren(): Flow<List<Child>> =
        childrenState.map { it.values.sortedBy { c -> c.childName } }

    override suspend fun getAllHouseholds(): List<Household> = householdsState.value.values.toList()

    override suspend fun getAllChildrenList(): List<Child> = childrenState.value.values.toList()

    override suspend fun getAllBaselines(): List<BaselineAssessment> = baselinesState.value.values.toList()

    override fun getAllBaselinesFlow(): Flow<List<BaselineAssessment>> =
        baselinesState.map { it.values.toList() }

    override suspend fun getAllFollowups(): List<LongitudinalFollowup> = followupsState.value

    override fun getAllFollowupsFlow(): Flow<List<LongitudinalFollowup>> = followupsState

    override fun childFlow(id: String): Flow<Child?> = childrenState.map { it[id] }

    override fun baselineFlow(id: String): Flow<BaselineAssessment?> = baselinesState.map { it[id] }

    override fun followupsFlow(id: String): Flow<List<LongitudinalFollowup>> =
        followupsState.map { list -> list.filter { it.studyId == id } }

    override suspend fun getFollowupByStudyIdAndRound(studyId: String, round: String): LongitudinalFollowup? =
        followupsState.value.find { it.studyId == studyId && it.followupRound == round }

    override suspend fun deleteFollowupsForChild(studyId: String) {
        followupsState.value = followupsState.value.filterNot { it.studyId == studyId }
    }

    override suspend fun deleteBaselineForChild(studyId: String) {
        baselinesState.value = baselinesState.value.toMutableMap().apply { remove(studyId) }
    }

    override suspend fun deleteChild(studyId: String) {
        childrenState.value = childrenState.value.toMutableMap().apply { remove(studyId) }
    }

    override suspend fun deleteOrphanedHouseholds() {
        val activeHouseholdIds = childrenState.value.values.map { it.householdId }.toSet()
        householdsState.value = householdsState.value.filterKeys { it in activeHouseholdIds }
    }

    override suspend fun insertFollowup(followup: LongitudinalFollowup) {
        val current = followupsState.value.toMutableList()
        if (followup.followupId == 0) {
            val assigned = followup.copy(followupId = nextFollowupId++)
            current.add(assigned)
        } else {
            val idx = current.indexOfFirst { it.followupId == followup.followupId }
            if (idx >= 0) {
                current[idx] = followup
            } else {
                current.add(followup)
            }
        }
        followupsState.value = current
    }

    override suspend fun insertDraft(draft: Draft) {
        draftsState.value = draftsState.value.toMutableMap().apply { put(draft.id, draft) }
    }

    override fun getAllDrafts(): Flow<List<Draft>> =
        draftsState.map { it.values.sortedByDescending { d -> d.timestamp } }

    override suspend fun deleteDraft(id: String) {
        draftsState.value = draftsState.value.toMutableMap().apply { remove(id) }
    }
}

class ExampleUnitTest {

    @Test
    fun simulateFullDataEntryAndVerifyNothingIsLost() = runBlocking {
        val dao = FakeSamDao()
        val repo = SamRepository(dao, transactionWrapper = dao.createTransactionWrapper())

        // 1. Populate every single Baseline field from QuestionBank
        val baselineFields = QuestionBank.baselineSections
            .flatMap { it.fields }
            .filter { it !is FormField.Subheading }

        val baselineAnswers = mutableMapOf<String, String>()
        baselineFields.forEachIndexed { idx, field ->
            baselineAnswers[field.id] = when (field) {
                is FormField.Dropdown -> field.optionsEn.firstOrNull() ?: "Option1"
                is FormField.CheckboxGroup -> field.optionsEn.take(2).joinToString(", ")
                is FormField.Text -> if (field.isNumber) "${idx + 1}" else "Value_${field.id}_\"quoted\", line1\nline2"
                else -> ""
            }
        }
        baselineAnswers["study_id"] = "S-1001"
        baselineAnswers["csam_id"] = "CSAM-5001"
        baselineAnswers["child_name"] = "Aarav Kumar"
        baselineAnswers["sex"] = "Male"
        baselineAnswers["awc_name"] = "AWC Rampur, Sector 4"
        baselineAnswers["block_district"] = "Phulwari / Patna"
        baselineAnswers["family_type"] = "Joint"
        baselineAnswers["total_members"] = "7"
        baselineAnswers["monthly_income"] = "8500.5"
        baselineAnswers["programme_outcome"] = "Defaulter"
        baselineAnswers["snp_provided"] = "Yes"
        baselineAnswers["snp_days"] = "6"
        baselineAnswers["where_care"] = "Government facility"
        baselineAnswers["time_care"] = "1–2 days"
        baselineAnswers["care_barriers"] = "Cost, Distance, Lack of transport"

        // Confirm Section 7 fields exist in both baseline and followup QuestionBank
        assertTrue(baselineFields.any { it.id == "where_care" })
        assertTrue(baselineFields.any { it.id == "time_care" })
        assertTrue(baselineFields.any { it.id == "care_barriers" })

        val hh1 = Household("HH-1001", "Joint", 7, 8500.5)
        val ch1 = Child("S-1001", "CSAM-5001", "HH-1001", "Aarav Kumar", "Male", "AWC Rampur, Sector 4", "Phulwari / Patna")
        val bs1 = BaselineAssessment(
            studyId = "S-1001",
            programmeOutcome = baselineAnswers["programme_outcome"]!!,
            thrReceivedTreatment = baselineAnswers["snp_provided"]!!,
            thrConsumedDays7d = 6,
            extraDataJson = JSONObject(baselineAnswers as Map<*, *>).toString()
        )
        repo.logBaseline(hh1, ch1, bs1)

        // 2. Populate every single Follow-up field for M9 (measured) and M12 (missing measurements = null)
        val followupFields = QuestionBank.followupSections
            .flatMap { it.fields }
            .filter { it !is FormField.Subheading }
        assertTrue(followupFields.any { it.id == "where_care" })
        assertTrue(followupFields.any { it.id == "time_care" })
        assertTrue(followupFields.any { it.id == "care_barriers" })

        val m9Answers = mutableMapOf<String, String>()
        followupFields.forEachIndexed { idx, field ->
            m9Answers[field.id] = when (field) {
                is FormField.Dropdown -> field.optionsEn.firstOrNull() ?: "Opt"
                is FormField.CheckboxGroup -> field.optionsEn.take(2).joinToString(", ")
                is FormField.Text -> if (field.isNumber) "${idx + 2}" else "Followup_${field.id}_\"notes\", comma"
                else -> ""
            }
        }
        m9Answers["study_id"] = "S-1001"
        m9Answers["followup_round"] = "M9"
        m9Answers["current_status"] = "Alive"
        m9Answers["weight"] = "7.4"
        m9Answers["height"] = "68.5"
        m9Answers["whz"] = "-2.4"
        m9Answers["snp_provided"] = "Yes"
        m9Answers["snp_days"] = "5"
        m9Answers["where_care"] = "Private healthcare provider"
        m9Answers["time_care"] = "Same day"
        m9Answers["care_barriers"] = "Cost, Lack of awareness"

        val followupM9 = LongitudinalFollowup(
            studyId = "S-1001",
            followupRound = "M9",
            currentStatus = "Alive",
            weight = 7.4,
            height = 68.5,
            whz = -2.4,
            thrReceived = "Yes",
            thrConsumedDays7d = 5,
            dietaryScore24h = 3,
            extraDataJson = JSONObject(m9Answers as Map<*, *>).toString()
        )
        repo.logFollowup(followupM9)

        // Test overwriting M9 preserves single row and updates weight
        m9Answers["weight"] = "7.6"
        m9Answers["whz"] = "-2.1"
        val updatedM9 = followupM9.copy(
            weight = 7.6,
            whz = -2.1,
            extraDataJson = JSONObject(m9Answers as Map<*, *>).toString()
        )
        repo.logFollowup(updatedM9)
        assertEquals(1, repo.getAllFollowups().size)
        assertEquals(7.6, repo.getFollowupByStudyIdAndRound("S-1001", "M9")?.weight ?: 0.0, 0.001)

        // Log M12 with null weight, height, and whz
        val m12Answers = HashMap(m9Answers).apply {
            put("followup_round", "M12")
            remove("weight")
            remove("height")
            remove("whz")
        }
        val followupM12 = LongitudinalFollowup(
            studyId = "S-1001",
            followupRound = "M12",
            currentStatus = "Alive",
            weight = null,
            height = null,
            whz = null,
            thrReceived = "No",
            thrConsumedDays7d = 0,
            dietaryScore24h = 2,
            extraDataJson = JSONObject(m12Answers as Map<*, *>).toString()
        )
        repo.logFollowup(followupM12)

        // 3. Verify live profile flow emits updated child, baseline, and both follow-ups
        val profileFlow = combine(
            repo.dao.childFlow("S-1001"),
            repo.dao.baselineFlow("S-1001"),
            repo.dao.followupsFlow("S-1001")
        ) { c, b, f -> c?.let { Triple(it, b, f) } }
        val profile = profileFlow.first()
        assertNotNull(profile)
        assertEquals("Aarav Kumar", profile!!.first.childName)
        assertEquals("Defaulter", profile.second?.programmeOutcome)
        assertEquals(2, profile.third.size)

        // 4. Test CSV Export & Import Roundtrip into a fresh repository
        val csvString = buildCsvExportString(
            households = repo.getAllHouseholds().associateBy { it.householdId },
            children = repo.getAllChildrenList(),
            baselines = repo.getAllBaselines().associateBy { it.studyId },
            followups = repo.getAllFollowups().groupBy { it.studyId }
        )
        assertTrue(csvString.isNotBlank())

        val csvDestDao = FakeSamDao()
        val csvDestRepo = SamRepository(csvDestDao, transactionWrapper = csvDestDao.createTransactionWrapper())
        val (csvImported, csvSkipped) = importDataFromCsvReader(StringReader(csvString), csvDestRepo)
        assertEquals(1, csvImported)
        assertEquals(0, csvSkipped)

        val restoredBaseline = csvDestRepo.getAllBaselines().single()
        val restoredBaseJson = JSONObject(restoredBaseline.extraDataJson)
        for (field in baselineFields) {
            assertEquals(
                "Mismatch on baseline field ${field.id}",
                baselineAnswers[field.id],
                restoredBaseJson.optString(field.id, "")
            )
        }

        val restoredM9 = csvDestRepo.getFollowupByStudyIdAndRound("S-1001", "M9")!!
        assertEquals(7.6, restoredM9.weight!!, 0.001)
        assertEquals(68.5, restoredM9.height!!, 0.001)
        assertEquals(-2.1, restoredM9.whz!!, 0.001)
        val restoredM9Json = JSONObject(restoredM9.extraDataJson)
        for (field in followupFields) {
            assertEquals(
                "Mismatch on M9 field ${field.id}",
                m9Answers[field.id],
                restoredM9Json.optString(field.id, "")
            )
        }

        val restoredM12 = csvDestRepo.getFollowupByStudyIdAndRound("S-1001", "M12")!!
        assertNull("Missing weight must remain null after CSV roundtrip", restoredM12.weight)
        assertNull("Missing height must remain null after CSV roundtrip", restoredM12.height)
        assertNull("Missing WHZ must remain null after CSV roundtrip", restoredM12.whz)

        // 5. Test JSON Backup & Restore Roundtrip into a fresh repository
        val jsonBackup = buildBackupJsonString(
            households = repo.getAllHouseholds(),
            children = repo.getAllChildrenList(),
            baselines = repo.getAllBaselines(),
            followups = repo.getAllFollowups()
        )
        val jsonDestDao = FakeSamDao()
        val jsonDestRepo = SamRepository(jsonDestDao, transactionWrapper = jsonDestDao.createTransactionWrapper())
        val (jsonImported, jsonSkipped) = restoreBackupFromJsonString(jsonBackup, jsonDestRepo)
        assertEquals(1, jsonImported)
        assertEquals(0, jsonSkipped)
        assertNull(jsonDestRepo.getFollowupByStudyIdAndRound("S-1001", "M12")!!.weight)
        assertEquals(7.6, jsonDestRepo.getFollowupByStudyIdAndRound("S-1001", "M9")!!.weight!!, 0.001)

        // 6. Test atomic rollback & CSAM ID clash error message when restoring a clashing CSAM ID
        val clashingJson = buildBackupJsonString(
            households = listOf(
                Household("HH-2001", "Nuclear", 4, 5000.0),
                Household("HH-2002", "Nuclear", 5, 6000.0)
            ),
            children = listOf(
                Child("S-2001", "CSAM-9001", "HH-2001", "Valid Child", "Female", "AWC 1", "Block 1"),
                Child("S-2002", "CSAM-5001", "HH-2002", "Clashing Child", "Male", "AWC 2", "Block 2")
            ),
            baselines = emptyList(),
            followups = emptyList()
        )
        try {
            restoreBackupFromJsonString(clashingJson, jsonDestRepo)
            fail("Expected SQLiteConstraintException due to CSAM-5001 clash")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("CSAM-5001") == true)
        }
        // Verify S-2001 was rolled back and only S-1001 remains
        assertEquals(1, jsonDestRepo.getAllChildrenList().size)
        assertNull(jsonDestRepo.getChildByStudyId("S-2001"))

        // 7. Test deleting a child also removes its orphaned household
        jsonDestRepo.deleteChildRecord("S-1001")
        assertTrue(jsonDestRepo.getAllChildrenList().isEmpty())
        assertTrue("Orphaned household should be deleted", jsonDestRepo.getAllHouseholds().isEmpty())
    }
}
