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
    data class ConfirmOverwrite(
        val followup: LongitudinalFollowup,
        val draftIdToDelete: String?,
        val message: String
    ) : FormState()
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
    val normal: Int = 0,
    val notYetMeasured: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SamRepository(database.samDao(), database)
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

    fun dismissOverwriteConfirmation() {
        _followupState.value = FormState.Idle
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
        var notYetMeasuredCount = 0

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
                    when (b?.programmeOutcome) {
                        "Recovered" -> normalCount++
                        "Died" -> { /* do not classify dead */ }
                        else -> notYetMeasuredCount++
                    }
                }
            } else {
                val b = baselines[child.studyId]
                when (b?.programmeOutcome) {
                    "Recovered" -> normalCount++
                    "Died" -> { /* do not classify dead */ }
                    else -> notYetMeasuredCount++
                }
            }
        }

        AnalyticsStats(
            totalEnrolled = children.size,
            male = children.count { it.sex.equals("Male", ignoreCase = true) },
            female = children.count { it.sex.equals("Female", ignoreCase = true) },
            sam = samCount,
            mam = mamCount,
            normal = normalCount,
            notYetMeasured = notYetMeasuredCount
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AnalyticsStats())

    fun submitFollowup(
        followup: LongitudinalFollowup,
        draftIdToDelete: String? = null,
        forceOverwrite: Boolean = false
    ) {
        viewModelScope.launch {
            _followupState.value = FormState.Loading
            try {
                if (!forceOverwrite) {
                    val existing = repository.getFollowupByStudyIdAndRound(followup.studyId, followup.followupRound)
                    if (existing != null) {
                        _followupState.value = FormState.ConfirmOverwrite(
                            followup = followup,
                            draftIdToDelete = draftIdToDelete,
                            message = "${followup.followupRound} already logged for this child. Overwrite?"
                        )
                        return@launch
                    }
                }
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

    fun getChildProfileData(studyId: String): kotlinx.coroutines.flow.Flow<Triple<Child, BaselineAssessment?, List<LongitudinalFollowup>>?> =
        kotlinx.coroutines.flow.combine(
            repository.dao.childFlow(studyId),
            repository.dao.baselineFlow(studyId),
            repository.dao.followupsFlow(studyId)
        ) { c, b, f -> c?.let { Triple(it, b, f) } }

    fun exportBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _operationStatus.value = OperationStatus.Loading
            try {
                val households = repository.getAllHouseholds()
                val children = repository.getAllChildrenList()
                val baselines = repository.getAllBaselines()
                val followups = repository.getAllFollowups()
                val jsonStr = buildBackupJsonString(households, children, baselines, followups)
                context.contentResolver.openOutputStream(uri)?.use { it.write(jsonStr.toByteArray(Charsets.UTF_8)) }
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
                val (importedChildren, skippedChildren) = restoreBackupFromJsonString(jsonString, repository)
                val msg = if (skippedChildren > 0) {
                    "Backup restored: $importedChildren children ($skippedChildren skipped due to blank Study/CSAM ID)."
                } else {
                    "Backup restored successfully ($importedChildren children)!"
                }
                _operationStatus.value = OperationStatus.Success(msg)
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
                val (imported, skipped) = importDataFromCsvImpl(context, uri, repository)
                val msg = if (skipped > 0) {
                    "CSV imported: $imported children ($skipped skipped due to blank Study/CSAM ID)."
                } else {
                    "CSV imported successfully ($imported children)!"
                }
                _operationStatus.value = OperationStatus.Success(msg)
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
