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

data class AnalyticsStats(
    val totalEnrolled: Int = 0,
    val male: Int = 0,
    val female: Int = 0,
    val sam: Int = 0,
    val mam: Int = 0,
    val normal: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {


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

    private val repository = SamRepository(AppDatabase.getDatabase(application).samDao())
    val drafts = repository.dao.getAllDrafts()
    
    init {
    }

    private val _baselineState = MutableStateFlow<FormState>(FormState.Idle)
    val baselineState: StateFlow<FormState> = _baselineState.asStateFlow()

    private val _followupState = MutableStateFlow<FormState>(FormState.Idle)
    val followupState: StateFlow<FormState> = _followupState.asStateFlow()

    private val _searchedChild = MutableStateFlow<Child?>(null)
    val searchedChild: StateFlow<Child?> = _searchedChild.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    fun resetBaselineState() {
        _baselineState.value = FormState.Idle
    }

    fun submitBaseline(
        household: Household,
        child: Child,
        baseline: BaselineAssessment // It has extraDataJson built in
    ) {
        viewModelScope.launch {
            _baselineState.value = FormState.Loading
            try {
                repository.logBaseline(household, child, baseline)
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
            try {
                val child = repository.searchChild(query)
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

    val analyticsStats: StateFlow<AnalyticsStats> = kotlinx.coroutines.flow.flow {
        repository.getAllChildren().collect { children ->
            val baselines = repository.getAllBaselines().associateBy { it.studyId }
            val followups = repository.getAllFollowups().groupBy { it.studyId }
            
            var samCount = 0
            var mamCount = 0
            var normalCount = 0
            
            for (child in children) {
                // Get most recent WHZ
                val fList = followups[child.studyId]
                if (!fList.isNullOrEmpty()) {
                    val latestWHZ = fList.maxByOrNull { it.followupRound }?.whz ?: 0.0 // M15 > M12 > M9 approx by string sort
                    if (latestWHZ < -3) samCount++
                    else if (latestWHZ < -2) mamCount++
                    else normalCount++
                } else {
                    val b = baselines[child.studyId]
                    if (b != null) {
                        // Estimate baseline from outcome if no WHZ is provided
                        when (b.programmeOutcome) {
                            "Recovered" -> normalCount++
                            // If they are in treatment we assume still SAM or MAM based on generic logic.
                            else -> samCount++
                        }
                    }
                }
            }
            
            emit(AnalyticsStats(
                totalEnrolled = children.size,
                male = children.count { it.sex.equals("Male", ignoreCase = true) },
                female = children.count { it.sex.equals("Female", ignoreCase = true) },
                sam = samCount,
                mam = mamCount,
                normal = normalCount
            ))
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AnalyticsStats())

    fun submitFollowup(followup: LongitudinalFollowup) {
        viewModelScope.launch {
            _followupState.value = FormState.Loading
            try {
                repository.logFollowup(followup)
                _followupState.value = FormState.Success
            } catch (e: Exception) {
                _followupState.value = FormState.Error(
                    if (e.message?.contains("UNIQUE constraint") == true) {
                        "This round is already logged for this child."
                    } else {
                        e.message ?: "Failed to log followup"
                    }
                )
            }
        }
    }

    suspend fun getChildProfileDataOnce(studyId: String): Triple<Child, BaselineAssessment?, List<LongitudinalFollowup>>? {
        val child = repository.searchChild(studyId)
        if (child != null) {
            val baselines = repository.getAllBaselines().filter { it.studyId == studyId }
            val followups = repository.getAllFollowups().filter { it.studyId == studyId }
            return Triple(child, baselines.firstOrNull(), followups)
        }
        return null
    }

    fun deleteRecord(studyId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val child = repository.searchChild(studyId)
            if (child != null) {
                repository.deleteChildRecord(child.studyId)
            }
        }
    }

    fun getChildProfileData(studyId: String): kotlinx.coroutines.flow.Flow<Triple<Child, BaselineAssessment?, List<LongitudinalFollowup>>?> = kotlinx.coroutines.flow.flow {
        val child = repository.searchChild(studyId)
        if (child != null) {
            val baselines = repository.getAllBaselines().filter { it.studyId == studyId }
            val followups = repository.getAllFollowups().filter { it.studyId == studyId }
            emit(Triple(child, baselines.firstOrNull(), followups))
        } else {
            emit(null)
        }
    }

    fun exportBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
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
                    obj.put("weight", f.weight)
                    obj.put("height", f.height)
                    obj.put("whz", f.whz)
                    obj.put("thrReceived", f.thrReceived)
                    obj.put("thrConsumedDays7d", f.thrConsumedDays7d)
                    obj.put("dietaryScore24h", f.dietaryScore24h)
                    obj.put("extraDataJson", f.extraDataJson)
                    fArray.put(obj)
                }
                json.put("followups", fArray)
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toString(2).toByteArray()) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

        fun importBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            importDataFromCsvImpl(context, uri, repository)
        }
    }

    fun exportDataToCsv(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val households = repository.getAllHouseholds().associateBy { it.householdId }
                val children = repository.getAllChildrenList()
                val baselines = repository.getAllBaselines().associateBy { it.studyId }
                val followups = repository.getAllFollowups().groupBy { it.studyId }
                exportDataToCsvImpl(context, uri, households, children, baselines, followups)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
