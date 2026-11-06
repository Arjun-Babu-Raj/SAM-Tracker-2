import re

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "r") as f:
    text = f.read()

bad_bh_fol = """    BackHandler {
        val toSave = dynamicAnswers.toMutableMap()
        if (searchedChild != null) {
            toSave["study_id"] = searchedChild!!.studyId
        }
        if (toSave.values.any { it.isNotBlank() }) {
            viewModel.saveDraft("followup", toSave, draftId)
        } else if (draftId != null) {
            viewModel.deleteDraft(draftId)
        }
        onNavigateBack()
    }"""

good_bh_fol = """    BackHandler {
        val toSave = dynamicAnswers.toMutableMap()
        if (searchedChild != null) {
            toSave["study_id"] = searchedChild!!.studyId
        }
        
        // Save the draft as long as there is any form data entered OR a child is already searched
        if (toSave.values.any { it.isNotBlank() }) {
            viewModel.saveDraft("followup", toSave, draftId)
        } else if (draftId != null) {
            viewModel.deleteDraft(draftId)
        }
        onNavigateBack()
    }"""

text = text.replace(bad_bh_fol, good_bh_fol)

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "w") as f:
    f.write(text)
