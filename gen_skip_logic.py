with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "r") as f:
    text = f.read()

import re

# We will replace the existing shouldShow block with a comprehensive one.
skip_logic = """        section.fields.forEach { field ->
            val shouldShow = when (field.id) {
                "caregiver_health_specify" -> answers["caregiver_health"] == "Yes"
                "migration_months" -> answers["migration_status"] != "No migration" && answers["migration_status"] != "Permanent migration"
                "referred_reason" -> answers["referred_nrc"] == "Yes"
                "attended_no_reason" -> answers["attended_visits"] == "No" || answers["attended_followups"] == "No"
                "fever_days" -> answers["fever"] == "Yes"
                "diarrhoea_episodes" -> answers["diarrhoea"] == "Yes"
                "cough_days" -> answers["cough"] == "Yes"
                "other_illness_specify" -> answers["other_illness"] == "Yes"
                "hosp_place", "hosp_reason" -> answers["hospitalization"] == "Yes"
                "exclusive_bf_months", "intro_before_6m" -> answers["exclusively_breastfed"] == "No"
                "bf_times_24h" -> answers["currently_breastfed"] == "Yes"
                "stop_bf_age" -> answers["currently_breastfed"] == "No"
                "difficulty_type" -> answers["difficulty_feeding"] == "Yes"
                "foods_avoided", "restriction_reason" -> answers["food_restricted"] == "Yes"
                "sam_signs" -> answers["heard_sam"] == "Yes"
                
                "date_dead", "cause_dead" -> answers["current_status"] == "Dead"
                "current_caregiver" -> answers["caregiver_changed"] == "Yes"
                "change_date" -> answers["outcome_since_last"] != "No change" && answers["outcome_since_last"] != null && answers["outcome_since_last"] != ""
                "reenrolled_when", "num_reenrolments", "retreatment_status", "retreatment_outcome" -> answers["reenrolled"] == "Yes"
                "referral_reason", "referral_status" -> answers["referred_nrc"] == "Yes"
                "where_care", "time_care", "care_barriers" -> answers["illness_experience"] == "Yes"
                "snp_not_consumed_reason" -> answers["snp_consumed"] == "Only a small amount" || answers["snp_consumed"] == "Did not consume"
                
                else -> true
            }
            if (!shouldShow) return@forEach
"""

text = re.sub(r'        section\.fields\.forEach \{ field ->\n            // Skip logic implementation.*?if \(!shouldShow\) return@forEach', skip_logic, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "w") as f:
    f.write(text)
