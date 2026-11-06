import re

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "r") as f:
    text = f.read()

logic = """    LaunchedEffect(searchedChild) {
        searchedChild?.let { child ->
            val profile = viewModel.getChildProfileDataOnce(child.studyId)
            val baseline = profile?.second
            var dataCollector = ""
            var ageMonths = ""
            var ageCategory = ""
            var baselineGroup = ""
            if (baseline != null) {
                try {
                    val json = JSONObject(baseline.extraDataJson)
                    dataCollector = json.optString("data_collector", "")
                    ageMonths = json.optString("child_age_months", "")
                    ageCategory = json.optString("age_category", "")
                    val outcome = baseline.programmeOutcome
                    if (outcome == "Recovered" || outcome == "Non-responder") {
                        baselineGroup = outcome
                    } else {
                        baselineGroup = json.optString("baseline_group", "")
                    }
                } catch (e: Exception) { }
            }
            
            dynamicAnswers = dynamicAnswers.toMutableMap().apply {
                if (this["study_id"].isNullOrEmpty()) this["study_id"] = child.studyId
                if (this["csam_id"].isNullOrEmpty()) this["csam_id"] = child.csamId
                if (this["child_name"].isNullOrEmpty()) this["child_name"] = child.childName
                if (this["data_collector"].isNullOrEmpty() && dataCollector.isNotEmpty()) this["data_collector"] = dataCollector
                if (this["child_age_months"].isNullOrEmpty() && ageMonths.isNotEmpty()) this["child_age_months"] = ageMonths
                if (this["age_category"].isNullOrEmpty() && ageCategory.isNotEmpty()) this["age_category"] = ageCategory
                if (this["baseline_group"].isNullOrEmpty() && baselineGroup.isNotEmpty()) this["baseline_group"] = baselineGroup
            }
        }
    }
"""

text = re.sub(r'    LaunchedEffect\(searchedChild\) \{.*?        \}\n    \}\n', logic, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "w") as f:
    f.write(text)
