sed -i 's/composable("baseline") {/composable("baseline?studyId={studyId}") { backStackEntry ->\
            val studyId = backStackEntry.arguments?.getString("studyId")/' app/src/main/java/com/example/MainActivity.kt

sed -i 's/BaselineScreen(/BaselineScreen(\n                studyId = studyId,/' app/src/main/java/com/example/MainActivity.kt
