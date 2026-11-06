import re

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "r") as f:
    text = f.read()

bad_logic = """                    "where_care", "time_care", "care_barriers" -> true"""

good_logic = """                    "where_care", "time_care", "care_barriers" -> (answers["illness_experience"] == "Yes" || answers["fever"] == "Yes" || answers["diarrhoea"] == "Yes" || answers["cough"] == "Yes" || answers["other_illness"] == "Yes")"""

text = text.replace(bad_logic, good_logic)

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "w") as f:
    f.write(text)
