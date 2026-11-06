import re

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "r") as f:
    text = f.read()

# Baseline fixes
text = text.replace(
    'FormField.Dropdown("food_consumed"',
    'FormField.CheckboxGroup("food_consumed"'
)
text = text.replace(
    'FormField.Dropdown("intro_before_6m"',
    'FormField.CheckboxGroup("intro_before_6m"'
)
text = text.replace(
    'FormField.Dropdown("dietary_groups"',
    'FormField.CheckboxGroup("dietary_groups"'
)
text = text.replace(
    'FormField.Dropdown("difficulty_type"',
    'FormField.CheckboxGroup("difficulty_type"'
)
text = text.replace(
    'FormField.Dropdown("foods_avoided"',
    'FormField.CheckboxGroup("foods_avoided"'
)
text = text.replace(
    'FormField.Dropdown("care_barriers"',
    'FormField.CheckboxGroup("care_barriers"'
)
text = text.replace(
    'FormField.Dropdown("sam_signs"',
    'FormField.CheckboxGroup("sam_signs"'
)

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "w") as f:
    f.write(text)
