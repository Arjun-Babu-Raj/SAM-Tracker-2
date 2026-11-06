package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import com.example.data.*

@Composable
fun FollowupScreen(draftId: String? = null, viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val searchedChild by viewModel.searchedChild.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val formState by viewModel.followupState.collectAsState()
    

    var dynamicAnswers by remember { mutableStateOf(mapOf<String, String>()) }
    var isHindi by remember { mutableStateOf(false) }

    LaunchedEffect(draftId) {
        if (!draftId.isNullOrEmpty()) {
            val draft = viewModel.getDraft(draftId)
            if (draft != null) {
                dynamicAnswers = draft
                val studyId = draft["study_id"]
                if (!studyId.isNullOrEmpty()) {
                    viewModel.searchChildForFollowup(studyId)
                }
            }
        }
    }



    LaunchedEffect(searchedChild) {
        searchedChild?.let { child ->
            val profile = viewModel.getChildProfileDataOnce(child.studyId)
            val baseline = profile?.second
            var dataCollector = ""
            var ageMonths = ""
            var ageCategory = ""
            var baselineGroup = ""
            if (baseline != null) {
                try {
                    val json = JSONObject(baseline.extraDataJson)
                    dataCollector = json.optString("data_collector", "")
                    ageMonths = json.optString("child_age_months", "")
                    ageCategory = json.optString("age_category", "")
                    val outcome = baseline.programmeOutcome
                    if (outcome == "Recovered" || outcome == "Non-responder") {
                        baselineGroup = outcome
                    } else {
                        baselineGroup = json.optString("baseline_group", "")
                    }
                } catch (e: Exception) { }
            }
            
            dynamicAnswers = dynamicAnswers.toMutableMap().apply {
                if (this["study_id"].isNullOrEmpty()) this["study_id"] = child.studyId
                if (this["csam_id"].isNullOrEmpty()) this["csam_id"] = child.csamId
                if (this["child_name"].isNullOrEmpty()) this["child_name"] = child.childName
                if (this["data_collector"].isNullOrEmpty() && dataCollector.isNotEmpty()) this["data_collector"] = dataCollector
                if (this["child_age_months"].isNullOrEmpty() && ageMonths.isNotEmpty()) this["child_age_months"] = ageMonths
                if (this["age_category"].isNullOrEmpty() && ageCategory.isNotEmpty()) this["age_category"] = ageCategory
                if (this["baseline_group"].isNullOrEmpty() && baselineGroup.isNotEmpty()) this["baseline_group"] = baselineGroup
            }
        }
    }

    BackHandler {
        val toSave = dynamicAnswers.toMutableMap()
        if (searchedChild != null) {
            toSave["study_id"] = searchedChild!!.studyId
        }
        
        // Save the draft as long as there is any form data entered OR a child is already searched
        if (toSave.values.any { it.isNotBlank() }) {
            viewModel.saveDraft("followup", toSave, draftId)
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
                text = if (isHindi) "टूल 2: फ़ॉलो-अप मात्रात्मक प्रपत्र" else "TOOL 2: FOLLOW-UP QUANTITATIVE PROFORMA",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Study ID, CSAM ID or Name") },
                trailingIcon = {
                    IconButton(onClick = { viewModel.searchChildForFollowup(searchQuery) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("search_bar")
            )
            
            searchError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (searchedChild != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Child Found: ${searchedChild!!.childName}", fontWeight = FontWeight.Bold)
                            Text("Study ID: ${searchedChild!!.studyId} | CSAM ID: ${searchedChild!!.csamId}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (formState is FormState.Success) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Success! Follow-up logged securely.", style = MaterialTheme.typography.titleMedium)
                            Button(
                                onClick = {
                                    viewModel.resetFollowupState()
                                    onNavigateBack()
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Return to Dashboard")
                            }
                        }
                    }
                } else { 
                    if (formState is FormState.Error) {
                        Text(
                            text = "Error: ${(formState as FormState.Error).message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    DynamicFormFields(
                        sections = QuestionBank.followupSections,
                        answers = dynamicAnswers,
                        onAnswerChange = { id, value ->
                            dynamicAnswers = dynamicAnswers.toMutableMap().apply { put(id, value) }
                        },
                        isHindi = isHindi
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                val toSave = dynamicAnswers.toMutableMap()
                                if (searchedChild != null) {
                                    toSave["study_id"] = searchedChild!!.studyId
                                }
                                if (toSave.values.any { it.isNotBlank() }) {
                                    viewModel.saveDraft("followup", toSave, draftId)
                                } else if (draftId != null) {
                                    viewModel.deleteDraft(draftId)
                                }
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f).height(56.dp).testTag("btn_save_draft"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(if (isHindi) "ड्राफ्ट सहेजें" else "Save Draft")
                        }
                        Button(
                            onClick = {
                                val round = dynamicAnswers["followup_round"] ?: ""
                                val currentStatus = dynamicAnswers["current_status"] ?: ""
                                val extraJson = JSONObject(dynamicAnswers).toString()
                                val followup = LongitudinalFollowup(
                                    studyId = searchedChild!!.studyId,
                                    followupRound = round,
                                    currentStatus = currentStatus,
                                    weight = dynamicAnswers["weight"]?.toDoubleOrNull() ?: 0.0,
                                    height = dynamicAnswers["height"]?.toDoubleOrNull() ?: 0.0,
                                    whz = dynamicAnswers["whz"]?.toDoubleOrNull() ?: 0.0,
                                    thrReceived = dynamicAnswers["snp_provided"] ?: "",
                                    thrConsumedDays7d = dynamicAnswers["snp_days"]?.toIntOrNull() ?: 0,
                                    dietaryScore24h = dynamicAnswers["dietary_groups"]?.split(",")?.filter { it.isNotBlank() }?.size ?: 0,
                                    extraDataJson = extraJson
                                )
                                viewModel.submitFollowup(followup)
                                if (draftId != null) { viewModel.deleteDraft(draftId) }
                            },
                            modifier = Modifier.weight(1f).height(56.dp).testTag("btn_save_followup"),
                            enabled = formState != FormState.Loading && dynamicAnswers["followup_round"].isNullOrBlank().not()
                        ) {
                            if (formState is FormState.Loading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Save")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Search for a child to unlock the form.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
