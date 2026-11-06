import re

with open("app/src/main/java/com/example/ui/ChildProfileScreen.kt", "r") as f:
    text = f.read()

# Replace weightEnrolment in the UI
text = text.replace(
    'val w1 = baseline.weightEnrolment',
    'val json = try { org.json.JSONObject(baseline.extraDataJson) } catch (e: Exception) { org.json.JSONObject() }\n                    val w1 = json.optDouble("weight_week1", 0.0)'
)

text = text.replace(
    'Pair(1f, baseline.weightEnrolment.toFloat()),',
    'Pair(1f, json.optDouble("weight_week1", 0.0).toFloat()),'
)

text = text.replace(
    'Pair(12f, baseline.weight12Weeks.toFloat())',
    'Pair(12f, json.optDouble("weight_week12", 0.0).toFloat())'
)

with open("app/src/main/java/com/example/ui/ChildProfileScreen.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

text = "import androidx.activity.compose.BackHandler\n" + text

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "w") as f:
    f.write(text)

