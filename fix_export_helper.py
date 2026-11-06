import re

with open("app/src/main/java/com/example/ui/ExportHelper.kt", "r") as f:
    text = f.read()

text = text.replace(
    'csvBuilder.append("Baseline_WeightEnrol,Baseline_Weight12,Baseline_HeightEnrol,Baseline_Height12,Baseline_Outcome,")',
    'csvBuilder.append("Baseline_Outcome,")'
)

text = text.replace(
    'csvBuilder.append("${b?.weightEnrolment ?: ""},${b?.weight12Weeks ?: ""},${b?.heightEnrolment ?: ""},${b?.height12Weeks ?: ""},${b?.programmeOutcome ?: ""},")',
    'csvBuilder.append("${b?.programmeOutcome ?: ""},")'
)

with open("app/src/main/java/com/example/ui/ExportHelper.kt", "w") as f:
    f.write(text)
