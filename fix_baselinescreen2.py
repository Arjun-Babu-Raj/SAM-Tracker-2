import re

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

# Fix the import issue
text = text.replace("import androidx.activity.compose.BackHandler\npackage com.example.ui", "package com.example.ui\nimport androidx.activity.compose.BackHandler\n")

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/ui/ChildProfileScreen.kt", "r") as f:
    text = f.read()

# Completely remove references to weightEnrolment by just parsing it again
text = re.sub(r'baseline\.weightEnrolment', r'json.optDouble("weight_week1", 0.0)', text)
text = re.sub(r'baseline\.weight12Weeks', r'json.optDouble("weight_week12", 0.0)', text)

with open("app/src/main/java/com/example/ui/ChildProfileScreen.kt", "w") as f:
    f.write(text)
