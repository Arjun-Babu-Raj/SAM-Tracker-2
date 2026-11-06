import re

with open("app/src/main/java/com/example/ui/AnalyticsScreen.kt", "r") as f:
    text = f.read()

# Update importLauncher to text/csv
text = text.replace('importLauncher.launch(arrayOf("application/json"))', 'importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv"))')

# I will also update the button text
text = text.replace('Text("Backup DB"', 'Text("Backup (JSON)"')
text = text.replace('Text("Restore DB"', 'Text("Restore (CSV)"')

with open("app/src/main/java/com/example/ui/AnalyticsScreen.kt", "w") as f:
    f.write(text)
