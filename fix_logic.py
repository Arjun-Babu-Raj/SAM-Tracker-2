import re

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "r") as f:
    text = f.read()

# Find the exact line matching "where_care", "time_care", "care_barriers" -> ...
text = re.sub(
    r'"where_care", "time_care", "care_barriers" -> .*?\n',
    '"where_care", "time_care", "care_barriers" -> answers["illness_experience"] != "No"\n',
    text
)

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "w") as f:
    f.write(text)
