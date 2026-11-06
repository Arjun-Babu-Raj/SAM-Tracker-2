import re

with open("app/src/main/java/com/example/ui/ExportHelper.kt", "r") as f:
    text = f.read()
text = text.replace("val bFields = QuestionBank.baselineSections.flatMap { it.fields }.map { it.id }", "val bFields = QuestionBank.baselineSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }")
text = text.replace("val fFields = QuestionBank.followupSections.flatMap { it.fields }.map { it.id }", "val fFields = QuestionBank.followupSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }")
with open("app/src/main/java/com/example/ui/ExportHelper.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/ui/ImportHelper.kt", "r") as f:
    text = f.read()
text = text.replace("val bFields = QuestionBank.baselineSections.flatMap { it.fields }.map { it.id }", "val bFields = QuestionBank.baselineSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }")
text = text.replace("val fFields = QuestionBank.followupSections.flatMap { it.fields }.map { it.id }", "val fFields = QuestionBank.followupSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }")
with open("app/src/main/java/com/example/ui/ImportHelper.kt", "w") as f:
    f.write(text)

