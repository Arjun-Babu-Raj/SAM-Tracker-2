with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "r") as f:
    text = f.read()

# Add a check before rendering the field
skip_logic = """
        section.fields.forEach { field ->
            // Skip logic implementation
            val shouldShow = when (field.id) {
                "foods_avoided", "restriction_reason" -> answers["food_restricted"] != "No"
                "reenrolled_when", "num_reenrolments", "retreatment_status", "retreatment_outcome" -> answers["reenrolled"] != "No"
                "referral_reason", "referral_status" -> answers["referred_nrc"] != "No"
                "where_care", "time_care", "care_barriers" -> answers["illness_experience"] != "No"
                else -> true
            }
            if (!shouldShow) return@forEach
            
            when (field) {
"""
text = text.replace("        section.fields.forEach { field ->\n            when (field) {", skip_logic)

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "w") as f:
    f.write(text)
