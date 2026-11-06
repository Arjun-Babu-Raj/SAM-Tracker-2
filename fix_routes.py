import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

# Make the routes use optional navArguments correctly
text = text.replace("import androidx.navigation.compose.rememberNavController",
                    "import androidx.navigation.compose.rememberNavController\nimport androidx.navigation.navArgument\nimport androidx.navigation.NavType")

old_baseline_route = """        composable("baseline?studyId={studyId}&draftId={draftId}") { backStackEntry ->
            val studyId = backStackEntry.arguments?.getString("studyId")
            val draftId = backStackEntry.arguments?.getString("draftId")"""
            
new_baseline_route = """        composable(
            "baseline?studyId={studyId}&draftId={draftId}",
            arguments = listOf(
                navArgument("studyId") { nullable = true; defaultValue = null },
                navArgument("draftId") { nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val studyId = backStackEntry.arguments?.getString("studyId")
            val draftId = backStackEntry.arguments?.getString("draftId")"""

text = text.replace(old_baseline_route, new_baseline_route)

old_followup_route = """        composable("followup?draftId={draftId}") { backStackEntry ->
            val draftId = backStackEntry.arguments?.getString("draftId")"""

new_followup_route = """        composable(
            "followup?draftId={draftId}",
            arguments = listOf(
                navArgument("draftId") { nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val draftId = backStackEntry.arguments?.getString("draftId")"""

text = text.replace(old_followup_route, new_followup_route)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
