with open("app/src/main/java/com/example/ui/QuestionBank.kt", "r") as f:
    text = f.read()

bad = """FormField.CheckboxGroup("care_barriers", "3. What were the main barriers to seeking care?", "3. इलाज तक पहुँचने में बाधाएँ:", listOf("Cost", "Distance", "Lack of transport", "Lack of Awareness", "Family decision", "Other"), listOf("खर्च", "दूरी", "यातायात की कमी", "जानकारी की कमी", "पारिवारिक निर्णय", "अन्य"))"""
good = """FormField.CheckboxGroup("care_barriers", "3. What were the main barriers to seeking care? (Tick all that apply)", "3. इलाज तक पहुँचने में बाधाएँ: (लागू होने वाले सभी पर टिक करें)", listOf("Cost", "Distance", "Lack of transport", "Lack of Awareness", "Family decision", "Other"), listOf("खर्च", "दूरी", "यातायात की कमी", "जानकारी की कमी", "पारिवारिक निर्णय", "अन्य"))"""

text = text.replace(bad, good)

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "w") as f:
    f.write(text)
