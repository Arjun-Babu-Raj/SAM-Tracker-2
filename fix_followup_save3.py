import re

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "r") as f:
    text = f.read()

bad_btn = """                            onClick = {
                                val toSave = dynamicAnswers.toMutableMap()
                                if (searchedChild != null) {
                                    toSave["study_id"] = searchedChild!!.studyId
                                }
                                viewModel.saveDraft("followup", toSave, draftId)
                                onNavigateBack()
                            },"""

good_btn = """                            onClick = {
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
                            },"""

text = text.replace(bad_btn, good_btn)

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "w") as f:
    f.write(text)
