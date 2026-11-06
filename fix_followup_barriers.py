import re

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "r") as f:
    text = f.read()

bad = 'FormField.CheckboxGroup("care_barriers", "4. What were the main barriers to seeking treatment?", "4. उपचार प्राप्त करने में मुख्य बाधाएँ क्या थीं?", listOf("Cost", "Distance", "Lack of transport", "Lack of awareness", "Family decision", "Other"), listOf("खर्च", "दूरी", "परिवहन की सुविधा का अभाव", "जानकारी का अभाव", "परिवार का निर्णय", "अन्य"))'
good = 'FormField.CheckboxGroup("care_barriers", "4. What were the main barriers to seeking treatment? (Tick all that apply)", "4. उपचार प्राप्त करने में मुख्य बाधाएँ क्या थीं? (लागू होने वाले सभी पर टिक करें)", listOf("Cost", "Distance", "Lack of transport", "Lack of awareness", "Family decision", "Other"), listOf("खर्च", "दूरी", "परिवहन की सुविधा का अभाव", "जानकारी का अभाव", "परिवार का निर्णय", "अन्य"))'

text = text.replace(bad, good)

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "w") as f:
    f.write(text)
