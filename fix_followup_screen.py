import re

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "r") as f:
    text = f.read()

# Add BackHandler import
if "import androidx.activity.compose.BackHandler" not in text:
    text = text.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.activity.compose.BackHandler")

# Add LaunchedEffect to prepopulate answers when searchedChild changes
if "LaunchedEffect(searchedChild)" not in text:
    prepopulate = """
    LaunchedEffect(searchedChild) {
        searchedChild?.let { child ->
            dynamicAnswers = dynamicAnswers.toMutableMap().apply {
                if (this["study_id"].isNullOrEmpty()) this["study_id"] = child.studyId
                if (this["csam_id"].isNullOrEmpty()) this["csam_id"] = child.csamId
                if (this["child_name"].isNullOrEmpty()) this["child_name"] = child.childName
            }
        }
    }
"""
    text = text.replace("    LazyColumn(", prepopulate + "    LazyColumn(")

# Add BackHandler
if "BackHandler {" not in text:
    backhandler = """
    BackHandler {
        val toSave = dynamicAnswers.toMutableMap()
        if (searchedChild != null) {
            toSave["study_id"] = searchedChild!!.studyId
        }
        viewModel.saveDraft("followup", toSave, draftId)
        onNavigateBack()
    }
"""
    text = text.replace("    LazyColumn(", backhandler + "    LazyColumn(")

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "w") as f:
    f.write(text)
