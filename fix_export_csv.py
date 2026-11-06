import re

with open("app/src/main/java/com/example/ui/ExportHelper.kt", "r") as f:
    text = f.read()

def quote_csv(match):
    return """val rawValue = json.optString(field, "")
                        val value = if (rawValue.contains(",")) "\\"${rawValue}\\"" else rawValue.replace("\\n", " ")"""

text = text.replace('val value = json.optString(field, "").replace(",", " ") // avoid csv break', 
"""val rawValue = json.optString(field, "")
                        val value = if (rawValue.contains(",")) "\\"${rawValue}\\"" else rawValue.replace("\\n", " ")""")

with open("app/src/main/java/com/example/ui/ExportHelper.kt", "w") as f:
    f.write(text)
