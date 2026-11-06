import re

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "r") as f:
    text = f.read()

# signature change
text = text.replace("fun FollowupScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {",
                    "fun FollowupScreen(draftId: String? = null, viewModel: MainViewModel, onNavigateBack: () -> Unit) {")

# add LaunchedEffect for drafts
effect = """
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
"""
text = text.replace("    var dynamicAnswers by remember { mutableStateOf(mapOf<String, String>()) }\n    var isHindi by remember { mutableStateOf(false) }", effect)

# Add save draft button
buttons = """                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                val toSave = dynamicAnswers.toMutableMap()
                                toSave["study_id"] = searchedChild!!.studyId
                                viewModel.saveDraft("followup", toSave, draftId)
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
                                    thrReceived = dynamicAnswers["thr_received"] ?: "",
                                    thrConsumedDays7d = dynamicAnswers["thr_days"]?.toIntOrNull() ?: 0,
                                    dietaryScore24h = dynamicAnswers["dietary_score"]?.toIntOrNull() ?: 0,
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
                    }"""

text = re.sub(r'Button\(\s*onClick = \{\s*val round = dynamicAnswers.*?Text\("Save Follow-up Data"\)\s*\}\s*\}', buttons, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "w") as f:
    f.write(text)
