import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

text = text.replace("import com.example.ui.FollowupScreen", "import com.example.ui.FollowupScreen\nimport com.example.ui.DraftsScreen")

drafts_route = """        composable("drafts") {
            DraftsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditDraft = { formType, draftId ->
                    val encodedId = android.net.Uri.encode(draftId)
                    if (formType == "baseline") {
                        navController.navigate("baseline?draftId=$encodedId")
                    } else {
                        navController.navigate("followup?draftId=$encodedId")
                    }
                }
            )
        }
"""

if "composable(\"drafts\")" not in text:
    text = text.replace('composable("followup") {', drafts_route + '        composable("followup?draftId={draftId}") { backStackEntry ->\n            val draftId = backStackEntry.arguments?.getString("draftId")\n')
    text = text.replace('FollowupScreen(\n                viewModel = viewModel,\n                onNavigateBack = { navController.popBackStack() }\n            )',
                        'FollowupScreen(\n                draftId = draftId,\n                viewModel = viewModel,\n                onNavigateBack = { navController.popBackStack() }\n            )')

# Also need to update baseline composable to accept draftId
text = text.replace('composable("baseline?studyId={studyId}") { backStackEntry ->', 'composable("baseline?studyId={studyId}&draftId={draftId}") { backStackEntry ->')
text = text.replace('val studyId = backStackEntry.arguments?.getString("studyId")', 'val studyId = backStackEntry.arguments?.getString("studyId")\n            val draftId = backStackEntry.arguments?.getString("draftId")')
text = text.replace('BaselineScreen(\n                studyId = studyId,\n                viewModel = viewModel,', 'BaselineScreen(\n                studyId = studyId,\n                draftId = draftId,\n                viewModel = viewModel,')

# Wait, the baseline destination needs to change from "baseline?studyId={studyId}" to "baseline?studyId={studyId}&draftId={draftId}"?
# No, we can just use two routes or make them optional parameters.
# In compose navigation, query parameters are optional by default if they are in the format "?param={param}".
# So "baseline?studyId={studyId}&draftId={draftId}" means BOTH are optional.
# I need to find the definition and replace it.

text = re.sub(r'composable\("baseline\?studyId=\{studyId\}"\)', 'composable("baseline?studyId={studyId}&draftId={draftId}")', text)
text = re.sub(r'composable\("followup"\)', 'composable("followup?draftId={draftId}")', text)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
