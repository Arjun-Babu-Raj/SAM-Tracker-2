import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

# Replace DraftsScreen invocation
drafts_code = """            DraftsScreen(
                viewModel = viewModel,
                onNavigateToHome = { navController.navigate("home") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } },
                onNavigateToRecords = { navController.navigate("records") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } },
                onNavigateToAnalytics = { navController.navigate("analytics") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } },
                onEditDraft = { formType, draftId ->
                    val encodedId = android.net.Uri.encode(draftId)
                    if (formType == "baseline") {
                        navController.navigate("baseline?draftId=$encodedId")
                    } else {
                        navController.navigate("followup?draftId=$encodedId")
                    }
                }
            )"""

text = re.sub(r'DraftsScreen\([\s\S]*?onEditDraft =.*?\}\s*\n\s*\)', drafts_code, text)

# We also need to make sure onNavigateToDrafts is passed to HomeScreen, RecordsScreen, AnalyticsScreen
nav_to_drafts = """onNavigateToDrafts = { navController.navigate("drafts") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } }"""

text = text.replace("onNavigateToAnalytics = { navController.navigate(\"analytics\") {\n                    popUpTo(\"home\") { saveState = true }\n                    launchSingleTop = true\n                    restoreState = true\n                } }", 
                    "onNavigateToAnalytics = { navController.navigate(\"analytics\") {\n                    popUpTo(\"home\") { saveState = true }\n                    launchSingleTop = true\n                    restoreState = true\n                } },\n                " + nav_to_drafts)

# Wait, AnalyticsScreen needs onNavigateToDrafts as well, which is after onNavigateToRecords
text = text.replace("onNavigateToRecords = { navController.navigate(\"records\") {\n                    popUpTo(\"home\") { saveState = true }\n                    launchSingleTop = true\n                    restoreState = true\n                } }", 
                    "onNavigateToRecords = { navController.navigate(\"records\") {\n                    popUpTo(\"home\") { saveState = true }\n                    launchSingleTop = true\n                    restoreState = true\n                } },\n                " + nav_to_drafts)

# Because this might do it multiple times for records/analytics, I will just fix the dupes.
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)

