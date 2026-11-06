with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "r") as f:
    text = f.read()

checkbox_impl = """                is FormField.CheckboxGroup -> {
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
"""

if "is FormField.CheckboxGroup" not in text:
    text = text.replace("            }\n        }\n    }\n}", f"{checkbox_impl}            }}\n        }}\n    }}\n}}")

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "w") as f:
    f.write(text)
