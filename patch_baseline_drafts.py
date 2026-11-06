import re

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

# Change BaselineScreen signature
text = text.replace("fun BaselineScreen(studyId: String? = null, viewModel: MainViewModel, onNavigateBack: () -> Unit) {",
                    "fun BaselineScreen(studyId: String? = null, draftId: String? = null, viewModel: MainViewModel, onNavigateBack: () -> Unit) {")

# Update LaunchedEffect to also load draft
effect_idx = text.find("LaunchedEffect(studyId)")
new_effect = """LaunchedEffect(studyId, draftId) {
        if (!draftId.isNullOrEmpty()) {
            val draft = viewModel.getDraft(draftId)
            if (draft != null) {
                dynamicAnswers = draft
            }
        } else if (!studyId.isNullOrEmpty()) {"""
text = text.replace("LaunchedEffect(studyId) {", new_effect)
text = text.replace("}\n            }\n        }\n    }", "}\n            }\n        }\n    }\n    }") # add closing brace for else if

# Add "Save as Draft" button before "Save Baseline Assessment" button
buttons = """                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                            val householdId = dynamicAnswers["household_id"] ?: ""
                            val familyType = dynamicAnswers["family_type"] ?: ""
                            val totalMembers = dynamicAnswers["total_members"]?.toIntOrNull() ?: 0
                            val monthlyIncome = dynamicAnswers["monthly_income"]?.toDoubleOrNull() ?: 0.0
                            val hl = Household(householdId, familyType, totalMembers, monthlyIncome)
                            
                            val studyId = dynamicAnswers["study_id"] ?: ""
                            val csamId = dynamicAnswers["csam_id"] ?: ""
                            val childName = dynamicAnswers["child_name"] ?: ""
                            val sex = dynamicAnswers["sex"] ?: ""
                            val awcName = dynamicAnswers["awc_name"] ?: ""
                            val blockDistrict = dynamicAnswers["block_district"] ?: ""
                            val ch = Child(studyId, csamId, householdId, childName, sex, awcName, blockDistrict)
                            
                            val extraJson = JSONObject(dynamicAnswers).toString()
                            val bs = BaselineAssessment(
                                studyId = studyId,
                                weightEnrolment = dynamicAnswers["weight_enrolment"]?.toDoubleOrNull() ?: 0.0,
                                weight12Weeks = dynamicAnswers["weight_12weeks"]?.toDoubleOrNull() ?: 0.0,
                                heightEnrolment = dynamicAnswers["height_enrolment"]?.toDoubleOrNull() ?: 0.0,
                                height12Weeks = dynamicAnswers["height_12weeks"]?.toDoubleOrNull() ?: 0.0,
                                programmeOutcome = dynamicAnswers["programme_outcome"] ?: "",
                                thrReceivedTreatment = dynamicAnswers["thr_received"] ?: "",
                                thrConsumedDays7d = dynamicAnswers["thr_days"]?.toIntOrNull() ?: 0,
                                dietaryScore24h = dynamicAnswers["dietary_score"]?.toIntOrNull() ?: 0,
                                extraDataJson = extraJson
                            )
                            viewModel.submitBaseline(hl, ch, bs)
                            if (draftId != null) { viewModel.deleteDraft(draftId) }
                        },
                        modifier = Modifier.weight(1f).height(56.dp).testTag("btn_save_baseline"),
                        enabled = formState != FormState.Loading && dynamicAnswers["study_id"].isNullOrBlank().not() && dynamicAnswers["household_id"].isNullOrBlank().not()
                    ) {
                        if (formState is FormState.Loading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (isHindi) "सहेजें" else "Save")
                        }
                    }
                }"""

text = re.sub(r'Button\(\s*onClick = \{\s*val householdId = dynamicAnswers.*?Text\(if \(isHindi\) "सहेजें" else "Save Baseline Assessment"\)\s*\}\s*\}', buttons, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "w") as f:
    f.write(text)
