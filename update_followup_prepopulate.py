import re

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "r") as f:
    text = f.read()

prepopulate_logic = """    LaunchedEffect(searchedChild) {
        searchedChild?.let { child ->
            val profile = viewModel.getChildProfileDataOnce(child.studyId)
            val baseline = profile?.second
            var dataCollector = ""
            if (baseline != null) {
                try {
                    val json = JSONObject(baseline.extraDataJson)
                    dataCollector = json.optString("data_collector", "")
                } catch (e: Exception) { }
            }
            
            dynamicAnswers = dynamicAnswers.toMutableMap().apply {
                if (this["study_id"].isNullOrEmpty()) this["study_id"] = child.studyId
                if (this["csam_id"].isNullOrEmpty()) this["csam_id"] = child.csamId
                if (this["child_name"].isNullOrEmpty()) this["child_name"] = child.childName
                if (this["data_collector"].isNullOrEmpty() && dataCollector.isNotEmpty()) this["data_collector"] = dataCollector
            }
        }
    }
"""

text = re.sub(r'    LaunchedEffect\(searchedChild\) \{.*?        \}\n    \}\n', prepopulate_logic, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "w") as f:
    f.write(text)
