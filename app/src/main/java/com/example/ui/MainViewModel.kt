package com.example.ui
import kotlinx.coroutines.withContext

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BaselineAssessment
import com.example.data.Child
import com.example.data.Household
import com.example.data.LongitudinalFollowup
import com.example.data.SamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class FormState {
    object Idle : FormState()
    object Loading : FormState()
    object Success : FormState()
    data class Error(val message: String) : FormState()
}

sealed class OperationStatus {
    object Idle : OperationStatus()
    object Loading : OperationStatus()
    data class Success(val message: String) : OperationStatus()
    data class Error(val message: String) : OperationStatus()
}

data class AnalyticsStats(
    val totalEnrolled: Int = 0,
    val male: Int = 0,
    val female: Int = 0,
    val sam: Int = 0,
    val mam: Int = 0,
    val normal: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SamRepository(AppDatabase.getDatabase(application).samDao())
    val drafts = repository.dao.getAllDrafts()

    private val _baselineState = MutableStateFlow<FormState>(FormState.Idle)
    val baselineState: StateFlow<FormState> = _baselineState.asStateFlow()

    private val _followupState = MutableStateFlow<FormState>(FormState.Idle)
    val followupState: StateFlow<FormState> = _followupState.asStateFlow()

    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val operationStatus: StateFlow<OperationStatus> = _operationStatus.asStateFlow()

    fun clearOperationStatus() {
        _operationStatus.value = OperationStatus.Idle
    }

    private val _searchedChild = MutableStateFlow<Child?>(null)
    val searchedChild: StateFlow<Child?> = _searchedChild.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    fun saveDraft(formType: String, data: Map<String, String>, draftId: String? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val id = draftId ?: java.util.UUID.randomUUID().toString()
            val json = org.json.JSONObject(data).toString()
            repository.dao.insertDraft(com.example.data.Draft(id, formType, System.currentTimeMillis(), json))
        }
    }

    fun deleteDraft(draftId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.dao.deleteDraft(draftId)
        }
    }

    suspend fun getDraft(draftId: String): Map<String, String>? {
        return withContext(kotlinx.coroutines.Dispatchers.IO) {
            val draft = repository.dao.getAllDrafts().firstOrNull()?.find { it.id == draftId }
            draft?.let {
                val json = org.json.JSONObject(it.dataJson)
                val map = mutableMapOf<String, String>()
                json.keys().forEach { key -> map[key] = json.getString(key) }
                map
            }
        }
    }

    fun resetBaselineState() {
        _baselineState.value = FormState.Idle
    }

    fun submitBaseline(
        household: Household,
        child: Child,
        baseline: BaselineAssessment,
        isEditMode: Boolean = false,
        draftIdToDelete: String? = null
    ) {
        viewModelScope.launch {
            _baselineState.value = FormState.Loading
            try {
                if (child.studyId.isBlank()) {
                    _baselineState.value = FormState.Error("Study ID cannot be empty.")
                    return@launch
                }
                if (child.csamId.isBlank()) {
                    _baselineState.value = FormState.Error("CSAM ID cannot be empty.")
                    return@launch
                }

                // Check duplicate Study ID if not editing
                val existingStudy = repository.getChildByStudyId(child.studyId)
                if (!isEditMode && existingStudy != null) {
                    _baselineState.value = FormState.Error("A child with Study ID '${child.studyId}' already exists.")
                    return@launch
                }

                // Check duplicate CSAM ID
                val existingCsam = repository.getChildByCsamId(child.csamId)
                if (existingCsam != null && existingCsam.studyId != child.studyId) {
                    _baselineState.value = FormState.Error("CSAM ID '${child.csamId}' is already assigned to child '${existingCsam.childName}' (Study ID: ${existingCsam.studyId}).")
                    return@launch
                }

                repository.logBaseline(household, child, baseline)
                if (!draftIdToDelete.isNullOrEmpty()) {
                    deleteDraft(draftIdToDelete)
                }
                _baselineState.value = FormState.Success
            } catch (e: Exception) {
                _baselineState.value = FormState.Error(e.message ?: "Failed to log baseline")
            }
        }
    }

    fun searchChildForFollowup(query: String) {
        viewModelScope.launch {
            _searchError.value = null
            _searchedChild.value = null
            val cleanQuery = query.trim()
            if (cleanQuery.isEmpty()) {
                _searchError.value = "Please enter a Study ID, CSAM ID or Name."
                return@launch
            }
            try {
                // Prioritize exact match on Study ID or CSAM ID
                val child = repository.searchChildExact(cleanQuery) ?: repository.searchChild(cleanQuery)
                if (child != null) {
                    _searchedChild.value = child
                } else {
                    _searchError.value = "Child not found. Verify Study ID or CSAM ID."
                }
            } catch (e: Exception) {
                _searchError.value = "Error searching database."
            }
        }
    }

    fun resetFollowupState() {
        _followupState.value = FormState.Idle
        _searchedChild.value = null
        _searchError.value = null
    }

    val allChildren: StateFlow<List<Child>> = repository.getAllChildren()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Helper to order rounds chronologically: M9 -> 9, M12 -> 12, M15 -> 15
    private fun getRoundWeight(round: String): Int {
        val num = round.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
        return num
    }

    val analyticsStats: StateFlow<AnalyticsStats> = kotlinx.coroutines.flow.combine(
        repository.getAllChildren(),
        repository.getAllFollowupsFlow(),
        repository.getAllBaselinesFlow()
    ) { children, followupsList, baselinesList ->
        val baselines = baselinesList.associateBy { it.studyId }
        val followups = followupsList.groupBy { it.studyId }

        var samCount = 0
        var mamCount = 0
        var normalCount = 0

        for (child in children) {
            val fList = followups[child.studyId]
            if (!fList.isNullOrEmpty()) {
                // Find chronologically latest round
                val latestFollowup = fList.maxByOrNull { getRoundWeight(it.followupRound) }
                // If child is marked Dead, do not classify nutritional status as SAM/MAM/Normal
                if (latestFollowup?.currentStatus.equals("Dead", ignoreCase = true)) {
                    continue
                }
                val latestWHZ = latestFollowup?.whz
                if (latestWHZ != null) {
                    when {
                        latestWHZ < -3.0 -> samCount++
                        latestWHZ < -2.0 -> mamCount++
                        else -> normalCount++
                    }
                } else {
                    // Check baseline if WHZ is missing in follow-up
                    val b = baselines[child.studyId]
                    if (b != null) {
                        when (b.programmeOutcome) {
                            "Recovered" -> normalCount++
                            "Died" -> { /* do not classify dead */ }
                            else -> samCount++
                        }
                    }
                }
            } else {
                val b = baselines[child.studyId]
                if (b != null) {
                    when (b.programmeOutcome) {
                        "Recovered" -> normalCount++
                        "Died" -> { /* do not classify dead */ }
                        else -> samCount++
                    }
                }
            }
        }

        AnalyticsStats(
            totalEnrolled = children.size,
            male = children.count { it.sex.equals("Male", ignoreCase = true) },
            female = children.count { it.sex.equals("Female", ignoreCase = true) },
            sam = samCount,
            mam = mamCount,
            normal = normalCount
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AnalyticsStats())

    fun submitFollowup(followup: LongitudinalFollowup, draftIdToDelete: String? = null) {
        viewModelScope.launch {
            _followupState.value = FormState.Loading
            try {
                repository.logFollowup(followup)
                if (!draftIdToDelete.isNullOrEmpty()) {
                    deleteDraft(draftIdToDelete)
                }
                _followupState.value = FormState.Success
            } catch (e: Exception) {
                _followupState.value = FormState.Error(e.message ?: "Failed to log followup")
            }
        }
    }

    suspend fun getChildProfileDataOnce(studyId: String): Triple<Child, BaselineAssessment?, List<LongitudinalFollowup>>? {
        val child = repository.getChildByStudyId(studyId) ?: repository.searchChildExact(studyId)
        if (child != null) {
            val baselines = repository.getAllBaselines().filter { it.studyId == child.studyId }
            val followups = repository.getAllFollowups().filter { it.studyId == child.studyId }
            return Triple(child, baselines.firstOrNull(), followups)
        }
        return null
    }

    fun deleteRecord(studyId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val child = repository.getChildByStudyId(studyId) ?: repository.searchChildExact(studyId)
            if (child != null) {
                repository.deleteChildRecord(child.studyId)
            }
        }
    }

    fun getChildProfileData(studyId: String): kotlinx.coroutines.flow.Flow<Triple<Child, BaselineAssessment?, List<LongitudinalFollowup>>?> = kotlinx.coroutines.flow.flow {
        val child = repository.getChildByStudyId(studyId) ?: repository.searchChildExact(studyId)
        if (child != null) {
            val baselines = repository.getAllBaselines().filter { it.studyId == child.studyId }
            val followups = repository.getAllFollowups().filter { it.studyId == child.studyId }
            emit(Triple(child, baselines.firstOrNull(), followups))
        } else {
            emit(null)
        }
    }

    fun exportBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _operationStatus.value = OperationStatus.Loading
            try {
                val households = repository.getAllHouseholds()
                val children = repository.getAllChildrenList()
                val baselines = repository.getAllBaselines()
                val followups = repository.getAllFollowups()
                val json = org.json.JSONObject()
                val hhArray = org.json.JSONArray()
                households.forEach { hh ->
                    val obj = org.json.JSONObject()
                    obj.put("householdId", hh.householdId)
                    obj.put("familyType", hh.familyType)
                    obj.put("totalFamilyMembers", hh.totalFamilyMembers)
                    obj.put("monthlyIncome", hh.monthlyIncome)
                    hhArray.put(obj)
                }
                json.put("households", hhArray)
                val chArray = org.json.JSONArray()
                children.forEach { ch ->
                    val obj = org.json.JSONObject()
                    obj.put("studyId", ch.studyId)
                    obj.put("csamId", ch.csamId)
                    obj.put("householdId", ch.householdId)
                    obj.put("childName", ch.childName)
                    obj.put("sex", ch.sex)
                    obj.put("awcName", ch.awcName)
                    obj.put("sectorBlockDistrict", ch.sectorBlockDistrict)
                    chArray.put(obj)
                }
                json.put("children", chArray)
                val bArray = org.json.JSONArray()
                baselines.forEach { b ->
                    val obj = org.json.JSONObject()
                    obj.put("studyId", b.studyId)
                    obj.put("programmeOutcome", b.programmeOutcome)
                    obj.put("thrReceivedTreatment", b.thrReceivedTreatment)
                    obj.put("thrConsumedDays7d", b.thrConsumedDays7d)
                    obj.put("extraDataJson", b.extraDataJson)
                    bArray.put(obj)
                }
                json.put("baselines", bArray)
                val fArray = org.json.JSONArray()
                followups.forEach { f ->
                    val obj = org.json.JSONObject()
                    obj.put("studyId", f.studyId)
                    obj.put("followupRound", f.followupRound)
                    obj.put("currentStatus", f.currentStatus)
                    f.weight?.let { obj.put("weight", it) }
                    f.height?.let { obj.put("height", it) }
                    f.whz?.let { obj.put("whz", it) }
                    obj.put("thrReceived", f.thrReceived)
                    obj.put("thrConsumedDays7d", f.thrConsumedDays7d)
                    obj.put("dietaryScore24h", f.dietaryScore24h)
                    obj.put("extraDataJson", f.extraDataJson)
                    fArray.put(obj)
                }
                json.put("followups", fArray)
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toString(2).toByteArray(Charsets.UTF_8)) }
                    ?: throw IllegalStateException("Could not write backup file.")
                _operationStatus.value = OperationStatus.Success("Backup JSON exported successfully.")
            } catch (e: Exception) {
                e.printStackTrace()
                _operationStatus.value = OperationStatus.Error("Backup failed: ${e.message}")
            }
        }
    }

    fun restoreBackupJson(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _operationStatus.value = OperationStatus.Loading
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Cannot read selected file.")
                val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val root = org.json.JSONObject(jsonString)

                val hhArray = root.optJSONArray("households")
                if (hhArray != null) {
                    for (i in 0 until hhArray.length()) {
                        val obj = hhArray.getJSONObject(i)
                        repository.insertHousehold(
                            Household(
                                householdId = obj.getString("householdId"),
                                familyType = obj.optString("familyType", ""),
                                totalFamilyMembers = obj.optInt("totalFamilyMembers", 0),
                                monthlyIncome = obj.optDouble("monthlyIncome", 0.0)
                            )
                        )
                    }
                }

                val chArray = root.optJSONArray("children")
                if (chArray != null) {
                    for (i in 0 until chArray.length()) {
                        val obj = chArray.getJSONObject(i)
                        repository.insertChild(
                            Child(
                                studyId = obj.getString("studyId"),
                                csamId = obj.optString("csamId", ""),
                                householdId = obj.getString("householdId"),
                                childName = obj.optString("childName", ""),
                                sex = obj.optString("sex", ""),
                                awcName = obj.optString("awcName", ""),
                                sectorBlockDistrict = obj.optString("sectorBlockDistrict", "")
                            )
                        )
                    }
                }

                val bArray = root.optJSONArray("baselines")
                if (bArray != null) {
                    for (i in 0 until bArray.length()) {
                        val obj = bArray.getJSONObject(i)
                        repository.insertBaseline(
                            BaselineAssessment(
                                studyId = obj.getString("studyId"),
                                programmeOutcome = obj.optString("programmeOutcome", ""),
                                thrReceivedTreatment = obj.optString("thrReceivedTreatment", ""),
                                thrConsumedDays7d = obj.optInt("thrConsumedDays7d", 0),
                                extraDataJson = obj.optString("extraDataJson", "{}")
                            )
                        )
                    }
                }

                val fArray = root.optJSONArray("followups")
                if (fArray != null) {
                    for (i in 0 until fArray.length()) {
                        val obj = fArray.getJSONObject(i)
                        val fw = if (obj.has("weight") && !obj.isNull("weight")) obj.getDouble("weight") else null
                        val fh = if (obj.has("height") && !obj.isNull("height")) obj.getDouble("height") else null
                        val fwhz = if (obj.has("whz") && !obj.isNull("whz")) obj.getDouble("whz") else null

                        repository.logFollowup(
                            LongitudinalFollowup(
                                studyId = obj.getString("studyId"),
                                followupRound = obj.getString("followupRound"),
                                currentStatus = obj.optString("currentStatus", "Alive"),
                                weight = fw,
                                height = fh,
                                whz = fwhz,
                                thrReceived = obj.optString("thrReceived", ""),
                                thrConsumedDays7d = obj.optInt("thrConsumedDays7d", 0),
                                dietaryScore24h = obj.optInt("dietaryScore24h", 0),
                                extraDataJson = obj.optString("extraDataJson", "{}")
                            )
                        )
                    }
                }
                _operationStatus.value = OperationStatus.Success("Backup restored successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
                _operationStatus.value = OperationStatus.Error("Restore JSON failed: ${e.message}")
            }
        }
    }

    fun importBackupCsv(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _operationStatus.value = OperationStatus.Loading
            try {
                importDataFromCsvImpl(context, uri, repository)
                _operationStatus.value = OperationStatus.Success("CSV imported successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
                _operationStatus.value = OperationStatus.Error("CSV Import failed: ${e.message}")
            }
        }
    }

    fun exportDataToCsv(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _operationStatus.value = OperationStatus.Loading
            try {
                val households = repository.getAllHouseholds().associateBy { it.householdId }
                val children = repository.getAllChildrenList()
                val baselines = repository.getAllBaselines().associateBy { it.studyId }
                val followups = repository.getAllFollowups().groupBy { it.studyId }
                exportDataToCsvImpl(context, uri, households, children, baselines, followups)
                _operationStatus.value = OperationStatus.Success("CSV exported successfully.")
            } catch (e: Exception) {
                e.printStackTrace()
                _operationStatus.value = OperationStatus.Error("CSV Export failed: ${e.message}")
            }
        }
    }
}
