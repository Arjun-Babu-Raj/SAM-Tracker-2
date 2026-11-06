import re

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "r") as f:
    text = f.read()

# Replace Dropdown with CheckboxGroup for specific fields
to_checkbox = [
    "major_event", "care_barriers", "foods_avoided", "difficulty_type", "dietary_groups",
    "sam_signs", "intro_before_6m", "food_consumed"
]

for field in to_checkbox:
    # Find the line declaring this field
    pattern = r'FormField\.Dropdown\("{}", (.*?\n?)'.format(field)
    text = re.sub(pattern, r'FormField.CheckboxGroup("{}", \1'.format(field), text)

# Now, we need to inject the Anthropometry weeks 1-12 into Section 4A.
# Currently, it has:
# FormField.Text("weight_enrolment", "Weight (kg) At enrolment (1st week):", "वज़न (कि.ग्रा.) नामांकन के समय (1st week):", true),
# FormField.Text("weight_12weeks", "Weight (kg) At 12 weeks:", "वज़न (कि.ग्रा.) 12 सप्ताह पर:", true),
# FormField.Text("height_enrolment", "Height/Length (cm) At enrolment:", "ऊँचाई/लंबाई (से.मी.) नामांकन के समय:", true),
# FormField.Text("height_12weeks", "Height/Length (cm) At 12 weeks:", "ऊँचाई/लंबाई (से.मी.) 12 सप्ताह पर:", true),
# FormField.Text("whz_enrolment", "WHZ At enrolment:", "WHZ नामांकन के समय:", true),
# FormField.Text("whz_12weeks", "WHZ At 12 weeks:", "WHZ 12 सप्ताह पर:", true),

# We will replace that whole block with a generated list of fields.
anthro_fields = []
for i in range(1, 13):
    en_label = f"Week {i}" if i > 1 else "At enrolment (1st week)"
    hi_label = f"सप्ताह {i}" if i > 1 else "नामांकन के समय (1st week)"
    
    anthro_fields.append(f'            FormField.Text("weight_week{i}", "Weight (kg) {en_label}:", "वज़न (कि.ग्रा.) {hi_label}:", true),')
    anthro_fields.append(f'            FormField.Text("height_week{i}", "Height/Length (cm) {en_label}:", "ऊँचाई/लंबाई (से.मी.) {hi_label}:", true),')
    anthro_fields.append(f'            FormField.Text("whz_week{i}", "WHZ {en_label}:", "WHZ {hi_label}:", true)')

anthro_str = "\n".join(anthro_fields) + ","

# We need to find the block to replace
start_str = 'FormField.Text("weight_enrolment"'
end_str = 'FormField.Text("whz_12weeks", "WHZ At 12 weeks:", "WHZ 12 सप्ताह पर:", true),'

idx1 = text.find(start_str)
idx2 = text.find(end_str) + len(end_str)

if idx1 != -1 and idx2 != -1:
    text = text[:idx1] + anthro_str + text[idx2:]
else:
    print("Could not find anthro block to replace!")

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "w") as f:
    f.write(text)
