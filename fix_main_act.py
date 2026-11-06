with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

text = text.replace("onNavigateToProfile = { studyId ->", """onNavigateToDrafts = { navController.navigate("drafts") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } },
                onNavigateToProfile = { studyId ->""")

text = text.replace("studyId = studyId,\n                viewModel = viewModel,", "studyId = studyId ?: \"\",\n                viewModel = viewModel,")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
