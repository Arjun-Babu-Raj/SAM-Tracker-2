import re

with open("app/src/main/java/com/example/ui/ChildProfileScreen.kt", "r") as f:
    text = f.read()

text = text.replace('json.optDouble("weight_week1", 0.0)', 'org.json.JSONObject(baseline?.extraDataJson ?: "{}").optDouble("weight_week1", 0.0)')
text = text.replace('json.optDouble("weight_week12", 0.0)', 'org.json.JSONObject(baseline?.extraDataJson ?: "{}").optDouble("weight_week12", 0.0)')

with open("app/src/main/java/com/example/ui/ChildProfileScreen.kt", "w") as f:
    f.write(text)
