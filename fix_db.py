import re

with open("app/src/main/java/com/example/data/Database.kt", "r") as f:
    text = f.read()

# Replace BaselineAssessment
new_baseline = """data class BaselineAssessment(
    @PrimaryKey val studyId: String,
    val programmeOutcome: String,
    val thrReceivedTreatment: String,
    val thrConsumedDays7d: Int,
    val extraDataJson: String
)"""

text = re.sub(r'data class BaselineAssessment\(.*?val extraDataJson: String\n\)', new_baseline, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/data/Database.kt", "w") as f:
    f.write(text)
