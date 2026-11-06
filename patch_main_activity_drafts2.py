import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

drafts_code = """DraftsScreen(
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

text = re.sub(r'DraftsScreen\([\s\S]*?onEditDraft =[\s\S]*?\}\s*\n\s*\)', drafts_code, text)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)

