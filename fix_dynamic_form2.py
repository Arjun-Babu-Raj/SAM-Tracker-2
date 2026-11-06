import re

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "r") as f:
    text = f.read()

bad_logic = """                    "where_care", "time_care", "care_barriers" -> (answers["illness_experience"] == "Yes" || answers["recent_illness"] == "Yes")"""

good_logic = """                    "where_care", "time_care", "care_barriers" -> true"""

text = text.replace(bad_logic, good_logic)

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "w") as f:
    f.write(text)
