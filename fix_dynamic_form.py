import re

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "r") as f:
    text = f.read()

bad_logic = """                    "referral_reason", "referral_status" -> answers["referred_nrc"] == "Yes"
                    "where_care", "time_care", "care_barriers" -> answers["illness_experience"] == "Yes"
                    "snp_not_consumed_reason" -> answers["snp_consumed"] == "Only a small amount" || answers["snp_consumed"] == "Did not consume"
                    else -> true
                }"""

good_logic = """                    "referral_reason", "referral_status" -> answers["referred_nrc"] == "Yes"
                    "where_care", "time_care", "care_barriers" -> (answers["illness_experience"] == "Yes" || answers["recent_illness"] == "Yes")
                    "snp_not_consumed_reason" -> answers["snp_consumed"] == "Only a small amount" || answers["snp_consumed"] == "Did not consume"
                    else -> true
                }"""

text = text.replace(bad_logic, good_logic)

with open("app/src/main/java/com/example/ui/DynamicFormFields.kt", "w") as f:
    f.write(text)
