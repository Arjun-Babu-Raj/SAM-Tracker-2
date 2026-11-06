import re

with open("app/src/main/java/com/example/ui/ImportHelper.kt", "r") as f:
    text = f.read()

new_b = """                    val b = BaselineAssessment(
                        studyId = studyId,
                        programmeOutcome = bOutcome,
                        thrReceivedTreatment = bJson.optString("snp_provided", ""),
                        thrConsumedDays7d = bJson.optString("snp_days", "0").toIntOrNull() ?: 0,
                        extraDataJson = bJson.toString()
                    )"""

text = re.sub(r'                    val b = BaselineAssessment\(.*?extraDataJson = bJson\.toString\(\)\n                    \)', new_b, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/ImportHelper.kt", "w") as f:
    f.write(text)
