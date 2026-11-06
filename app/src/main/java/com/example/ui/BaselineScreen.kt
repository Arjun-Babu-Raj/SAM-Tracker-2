package com.example.ui
import androidx.activity.compose.BackHandler


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import com.example.data.*
import androidx.compose.ui.Alignment

@Composable
fun BaselineScreen(studyId: String? = null, draftId: String? = null, viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val isEditMode = !studyId.isNullOrEmpty()
    var dynamicAnswers by remember {
        mutableStateOf<Map<String, String>>(if (studyId != null && studyId.isNotEmpty()) mapOf("study_id" to studyId) else emptyMap())
    }
    var isHindi by remember { mutableStateOf(false) }
    val formState by viewModel.baselineState.collectAsState()
    LaunchedEffect(studyId, draftId) {
        if (!draftId.isNullOrEmpty()) {
            val draft = viewModel.getDraft(draftId)
            if (draft != null) {
                dynamicAnswers = draft
            }
        } else if (!studyId.isNullOrEmpty()) {
            val childData = viewModel.getChildProfileDataOnce(studyId)
            if (childData != null) {
                val (child, baseline, _) = childData
                val map = mutableMapOf<String, String>()
                if (baseline != null) {
                    try {
                        val json = JSONObject(baseline.extraDataJson)
                        json.keys().forEach { key -> map[key] = json.getString(key) }
                    } catch (e: Exception) { e.printStackTrace() }
                }
                map["study_id"] = child.studyId
                if (map["csam_id"].isNullOrBlank()) map["csam_id"] = child.csamId
                if (map["household_id"].isNullOrBlank()) map["household_id"] = child.householdId
                if (map["child_name"].isNullOrBlank()) map["child_name"] = child.childName
                if (map["sex"].isNullOrBlank()) map["sex"] = child.sex
                if (map["awc_name"].isNullOrBlank()) map["awc_name"] = child.awcName
                if (map["block_district"].isNullOrBlank()) map["block_district"] = child.sectorBlockDistrict
                dynamicAnswers = map
            }
        }
    }

    if (formState is FormState.Success) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(if (isHindi) "सफलतापूर्वक सहेजा गया!" else "Successfully Saved!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.resetBaselineState() }) {
                Text(if (isHindi) "नया फॉर्म भरें" else "Submit Another")
            }
        }
    } else {
        BackHandler {
        if (dynamicAnswers.values.any { it.isNotBlank() }) {
            viewModel.saveDraft("baseline", dynamicAnswers, draftId)
        } else if (draftId != null) {
            viewModel.deleteDraft(draftId)
        }
        onNavigateBack()
    }
    
    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("English")
                    Switch(checked = isHindi, onCheckedChange = { isHindi = it }, modifier = Modifier.padding(horizontal = 8.dp))
                    Text("हिंदी")
                }
                
                Text(
                    text = if (isHindi) "टूल 1: आधारभूत (बेसलाइन) मात्रात्मक प्रश्नावली" else "TOOL 1: BASELINE QUANTITATIVE QUESTIONNAIRE",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (formState is FormState.Error) {
                    Text(
                        text = "Error: ${(formState as FormState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                DynamicFormFields(
                    sections = QuestionBank.baselineSections,
                    answers = dynamicAnswers,
                    onAnswerChange = { id, value ->
                        if (!(isEditMode && id == "study_id")) {
                            dynamicAnswers = dynamicAnswers.toMutableMap().apply { put(id, value) }
                        }
                    },
                    isHindi = isHindi,
                    readOnlyFields = if (isEditMode) setOf("study_id") else emptySet()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            viewModel.saveDraft("baseline", dynamicAnswers, draftId)
                            onNavigateBack()
                        },
                        modifier = Modifier.weight(1f).height(56.dp).testTag("btn_save_draft"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(if (isHindi) "ड्राफ्ट सहेजें" else "Save Draft")
                    }
                    Button(
                        onClick = {
                            val targetStudyId = if (isEditMode) studyId!! else (dynamicAnswers["study_id"] ?: "")
                            val householdId = dynamicAnswers["household_id"]?.takeIf { it.isNotBlank() } ?: targetStudyId
                            val familyType = dynamicAnswers["family_type"] ?: ""
                            val totalMembers = dynamicAnswers["total_members"]?.toIntOrNull() ?: 0
                            val monthlyIncome = dynamicAnswers["monthly_income"]?.toDoubleOrNull() ?: 0.0
                            val hl = Household(householdId, familyType, totalMembers, monthlyIncome)
                            
                            val csamId = dynamicAnswers["csam_id"] ?: ""
                            val childName = dynamicAnswers["child_name"] ?: ""
                            val sex = dynamicAnswers["sex"] ?: ""
                            val awcName = dynamicAnswers["awc_name"] ?: ""
                            val blockDistrict = dynamicAnswers["block_district"] ?: ""
                            val ch = Child(targetStudyId, csamId, householdId, childName, sex, awcName, blockDistrict)
                            
                            val answersWithStudyId = dynamicAnswers.toMutableMap().apply { put("study_id", targetStudyId) }
                            val extraJson = JSONObject(answersWithStudyId).toString()
                            val bs = BaselineAssessment(
                                studyId = targetStudyId,
                                programmeOutcome = dynamicAnswers["programme_outcome"] ?: "",
                                thrReceivedTreatment = dynamicAnswers["snp_provided"] ?: "",
                                thrConsumedDays7d = dynamicAnswers["snp_days"]?.toIntOrNull() ?: 0,
                                extraDataJson = extraJson
                            )
                            viewModel.submitBaseline(hl, ch, bs, isEditMode = isEditMode, draftIdToDelete = draftId)
                        },
                        modifier = Modifier.weight(1f).height(56.dp).testTag("btn_save_baseline"),
                        enabled = formState != FormState.Loading && (isEditMode || dynamicAnswers["study_id"].isNullOrBlank().not())
                    ) {
                        if (formState is FormState.Loading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (isHindi) "सहेजें" else "Save")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
