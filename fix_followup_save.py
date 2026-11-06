import re

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "r") as f:
    text = f.read()

bad_btn = """                        Button(
                            onClick = {
                                val toSave = dynamicAnswers.toMutableMap()
                                toSave["study_id"] = searchedChild!!.studyId
                                viewModel.saveDraft("followup", toSave, draftId)
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f).height(56.dp).testTag("btn_save_draft"),"""

good_btn = """                        Button(
                            onClick = {
                                val toSave = dynamicAnswers.toMutableMap()
                                if (searchedChild != null) {
                                    toSave["study_id"] = searchedChild!!.studyId
                                }
                                viewModel.saveDraft("followup", toSave, draftId)
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f).height(56.dp).testTag("btn_save_draft"),"""

text = text.replace(bad_btn, good_btn)

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "w") as f:
    f.write(text)
