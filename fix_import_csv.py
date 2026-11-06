import re

with open("app/src/main/java/com/example/ui/ImportHelper.kt", "r") as f:
    text = f.read()

split_logic = """            val cols = mutableListOf<String>()
            var current = java.lang.StringBuilder()
            var inQuotes = false
            for (char in line) {
                if (char == '"') {
                    inQuotes = !inQuotes
                } else if (char == ',' && !inQuotes) {
                    cols.add(current.toString().trim())
                    current = java.lang.StringBuilder()
                } else {
                    current.append(char)
                }
            }
            cols.add(current.toString().trim())"""

text = text.replace('val cols = line.split(",") // Basic CSV split. Assuming no commas in values since we replaced them with spaces in export', split_logic)

with open("app/src/main/java/com/example/ui/ImportHelper.kt", "w") as f:
    f.write(text)
