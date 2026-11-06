package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicFormFields(
    sections: List<FormSection>,
    answers: Map<String, String>,
    onAnswerChange: (String, String) -> Unit,
    isHindi: Boolean = false
) {
    Column {
        sections.forEach { section ->
            val titleText = if (isHindi) section.titleHi else section.titleEn
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            section.fields.forEach { field ->
                val shouldShow = when (field.id) {
                    "caregiver_health_specify" -> answers["caregiver_health"] == "Yes"
                    "migration_months" -> answers["migration_status"] != "No migration" && answers["migration_status"] != "Permanent migration"
                    "referred_reason" -> answers["referred_nrc"] == "Yes"
                    "attended_no_reason" -> answers["attended_visits"] == "No" || answers["attended_followups"] == "No"
                    "fever_days" -> answers["fever"] == "Yes"
                    "diarrhoea_episodes" -> answers["diarrhoea"] == "Yes"
                    "cough_days" -> answers["cough"] == "Yes"
                    "other_illness_specify" -> answers["other_illness"] == "Yes"
                    "hosp_place", "hosp_reason" -> answers["hospitalization"] == "Yes"
                    "exclusive_bf_months", "intro_before_6m" -> answers["exclusively_breastfed"] == "No"
                    "bf_times_24h" -> answers["currently_breastfed"] == "Yes"
                    "stop_bf_age" -> answers["currently_breastfed"] == "No"
                    "difficulty_type" -> answers["difficulty_feeding"] == "Yes"
                    "foods_avoided", "restriction_reason" -> answers["food_restricted"] == "Yes"
                    "sam_signs" -> answers["heard_sam"] == "Yes"
                    "date_dead", "cause_dead" -> answers["current_status"] == "Dead"
                    "current_caregiver" -> answers["caregiver_changed"] == "Yes"
                    "change_date" -> answers["outcome_since_last"] != "No change" && answers["outcome_since_last"] != null && answers["outcome_since_last"] != ""
                    "reenrolled_when", "num_reenrolments", "retreatment_status", "retreatment_outcome" -> answers["reenrolled"] == "Yes"
                    "referral_reason", "referral_status" -> answers["referred_nrc"] == "Yes"
                    "where_care", "time_care", "care_barriers" -> answers["illness_experience"] != "No"
                    "snp_not_consumed_reason" -> answers["snp_consumed"] == "Only a small amount" || answers["snp_consumed"] == "Did not consume"
                    else -> true
                }
                
                if (shouldShow) {
                    when (field) {
                        is FormField.Subheading -> {
                            val labelText = if (isHindi) field.labelHi else field.labelEn
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 1.dp)
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                            )
                        }
                        is FormField.Text -> {
                            val labelText = if (isHindi) field.labelHi else field.labelEn
                            val keyboardOptions = if (field.isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
                            OutlinedTextField(
                                value = answers[field.id] ?: "",
                                onValueChange = { onAnswerChange(field.id, it) },
                                label = { Text(labelText) },
                                keyboardOptions = keyboardOptions,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )
                        }
                        is FormField.Dropdown -> {
                            var expanded by remember { mutableStateOf(false) }
                            val selectedValue = answers[field.id] ?: ""
                            val selectedIndex = field.optionsEn.indexOf(selectedValue)
                            
                            val displayValue = if (selectedIndex >= 0) {
                                if (isHindi) field.optionsHi[selectedIndex] else field.optionsEn[selectedIndex]
                            } else selectedValue
                            
                            val labelText = if (isHindi) field.labelHi else field.labelEn
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = displayValue,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(labelText) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    field.optionsEn.indices.forEach { index ->
                                        val optionText = if (isHindi) field.optionsHi[index] else field.optionsEn[index]
                                        DropdownMenuItem(
                                            text = { Text(optionText) },
                                            onClick = {
                                                onAnswerChange(field.id, field.optionsEn[index])
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        is FormField.CheckboxGroup -> {
                            val labelText = if (isHindi) field.labelHi else field.labelEn
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            val selectedOptions = (answers[field.id] ?: "").split(",").filter { it.isNotBlank() }.toMutableSet()
                            
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                field.optionsEn.indices.forEach { index ->
                                    val optionEn = field.optionsEn[index]
                                    val optionHi = field.optionsHi[index]
                                    val optionText = if (isHindi) optionHi else optionEn
                                    val isChecked = selectedOptions.contains(optionEn)
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    selectedOptions.add(optionEn)
                                                } else {
                                                    selectedOptions.remove(optionEn)
                                                }
                                                onAnswerChange(field.id, selectedOptions.joinToString(","))
                                            }
                                        )
                                        Text(text = optionText, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
