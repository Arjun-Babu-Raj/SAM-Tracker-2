import re

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "r") as f:
    text = f.read()

for i in range(1, 13):
    text = text.replace(f'", true)\n            FormField.Text("weight_week{i+1}"', f'", true),\n            FormField.Text("weight_week{i+1}"')

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "w") as f:
    f.write(text)
