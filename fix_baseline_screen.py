import re

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

new_bs = """                            val bs = BaselineAssessment(
                                studyId = studyId,
                                programmeOutcome = dynamicAnswers["programme_outcome"] ?: "",
                                thrReceivedTreatment = dynamicAnswers["snp_provided"] ?: "",
                                thrConsumedDays7d = dynamicAnswers["snp_days"]?.toIntOrNull() ?: 0,
                                extraDataJson = extraJson
                            )"""

text = re.sub(r'                            val bs = BaselineAssessment\(.*?extraDataJson = extraJson\n                            \)', new_bs, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "w") as f:
    f.write(text)
