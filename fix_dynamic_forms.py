with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "r") as f:
    text = f.read()

subheading = """                is FormField.Subheading -> {
                    val labelText = if (isHindi) field.labelHi else field.labelEn
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                }"""

import re
text = re.sub(r'                is FormField\.Subheading -> \{\s*val labelText.*?modifier = Modifier\.padding\(top = 12\.dp, bottom = 4\.dp\)\s*\n\s*\}', subheading, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "w") as f:
    f.write(text)
