import re

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

bad_bh_base = """    BackHandler {
        viewModel.saveDraft("baseline", dynamicAnswers, draftId)
        onNavigateBack()
    }"""

good_bh_base = """    BackHandler {
        if (dynamicAnswers.values.any { it.isNotBlank() }) {
            viewModel.saveDraft("baseline", dynamicAnswers, draftId)
        } else if (draftId != null) {
            viewModel.deleteDraft(draftId)
        }
        onNavigateBack()
    }"""

text = text.replace(bad_bh_base, good_bh_base)

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "r") as f:
    text = f.read()

bad_bh_fol = """    BackHandler {
        val toSave = dynamicAnswers.toMutableMap()
        if (searchedChild != null) {
            toSave["study_id"] = searchedChild!!.studyId
        }
        viewModel.saveDraft("followup", toSave, draftId)
        onNavigateBack()
    }"""

good_bh_fol = """    BackHandler {
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

text = text.replace(bad_bh_fol, good_bh_fol)

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "w") as f:
    f.write(text)

