with open("app/src/main/java/com/example/ui/QuestionBank.kt", "r") as f:
    text = f.read()

text = text.replace(
    "class Dropdown(id: String, labelEn: String, labelHi: String, val optionsEn: List<String>, val optionsHi: List<String>) : FormField(id, labelEn, labelHi)",
    "class Dropdown(id: String, labelEn: String, labelHi: String, val optionsEn: List<String>, val optionsHi: List<String>) : FormField(id, labelEn, labelHi)\n    class CheckboxGroup(id: String, labelEn: String, labelHi: String, val optionsEn: List<String>, val optionsHi: List<String>) : FormField(id, labelEn, labelHi)"
)

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "w") as f:
    f.write(text)
