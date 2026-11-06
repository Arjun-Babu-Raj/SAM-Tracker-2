content = """    val followupSections = listOf(
        FormSection("SECTION 1: IDENTIFICATION", "खंड 1: पहचान", listOf(
            FormField.Text("study_id", "1. Study ID:", "1. अध्ययन क्रमांक:"),
            FormField.Text("csam_id", "2. CSAM ID:", "2. CSAM क्रमांक:"),
            FormField.Dropdown("followup_round", "3. Follow-up round:", "3. फ़ॉलो-अप चरण:", listOf("M9", "M12", "M15"), listOf("M9", "M12", "M15")),
            FormField.Text("date_assessment", "4. Date of assessment:", "4. आकलन की तारीख़:"),
            FormField.Dropdown("season", "5. Current season:", "5. वर्तमान मौसम:", listOf("Summer", "Monsoon", "Winter"), listOf("गर्मी", "बरसात", "सर्दी")),
            FormField.Text("child_name", "6. child name:", "6. बच्चे का नाम:"),
            FormField.Text("child_age_months", "7. Age (in completed months):", "7. आयु (पूरे महीनों में):", true),
            FormField.Dropdown("age_category", "8. Age category:", "8. आयु वर्ग:", listOf("6–23 months", "24–59 months"), listOf("6–23 महीने", "24–59 महीने")),
            FormField.Dropdown("baseline_group", "9. Baseline group:", "9. बेसलाइन समूह:", listOf("Recovered", "Non-responder"), listOf("स्वस्थ हुआ", "सुधार नहीं हुआ")),
            FormField.Text("data_collector", "10. Data collector name :", "10. आँकड़ा संग्राहक का नाम :"),
            FormField.Dropdown("consent", "11. Consent reaffirmed this round:", "11. इस चरण में सहमति पुनः पुष्टि:", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Dropdown("current_status", "12. Current status:", "12. वर्तमान स्थिति:", listOf("Alive", "Dead"), listOf("जीवित", "मृत")),
            FormField.Text("date_dead", "If dead: Date:", "यदि मृत: तारीख़:"),
            FormField.Text("cause_dead", "If dead: Cause:", "यदि मृत: कारण:")
        )),
        FormSection("SECTION 2: SOCIODEMOGRAPHIC CHARACTERISTICS", "खंड 2: सामाजिक-जनसांख्यिकीय विवरण", listOf(
            FormField.Dropdown("caregiver_changed", "1. Since the last visit, has the child's primary caregiver changed?", "1. पिछली मुलाक़ात के बाद क्या बच्चे का मुख्य देखभालकर्ता बदला?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Dropdown("current_caregiver", "If yes, current caregiver:", "यदि हाँ, वर्तमान देखभालकर्ता:", listOf("Mother", "Father", "Grandmother", "Grandfather", "Other"), listOf("माता", "पिता", "दादी/नानी", "दादा/नाना", "अन्य")),
            FormField.Dropdown("household_migrated", "2. Since the last visit, has the household migrated temporarily?", "2. पिछली मुलाक़ात के बाद क्या परिवार अस्थायी रूप से पलायन कर गया?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.CheckboxGroup("major_event", "3. Since the last visit, has any major household event occurred?", "3. पिछली मुलाक़ात के बाद क्या कोई बड़ी पारिवारिक घटना हुई?", listOf("Serious illness", "Death in family", "Income loss", "Migration", "None", "Other"), listOf("गंभीर बीमारी", "परिवार में मृत्यु", "आय में हानि", "पलायन", "कोई नहीं", "अन्य"))
        )),
        FormSection("SECTION 3: CSAM TREATMENT & PROGRAMME STATUS", "खंड 3: CSAM उपचार एवं कार्यक्रम स्थिति", listOf(
            FormField.Subheading("A. Anthropometry & Clinical Status", "शारीरिक माप एवं चिकित्सीय स्थिति"),
            FormField.Text("weight", "Weight:", "वज़न:", true),
            FormField.Text("height", "Height:", "ऊँचाई/लंबाई:", true),
            FormField.Text("whz", "WHZ:", "WHZ:", true),
            FormField.Dropdown("oedema", "1. Bilateral pitting oedema:", "1. दोनों पैरों में दबाने पर गड्ढा (सूजन):", listOf("Present", "Absent"), listOf("है", "नहीं")),
            FormField.Dropdown("appetite_change", "2. Any recent change in appetite since last visit?", "2. पिछली मुलाक़ात के बाद भूख में कोई हालिया बदलाव?", listOf("Improved", "Same", "Reduced"), listOf("बेहतर हुई", "समान", "घटी")),
            FormField.Dropdown("nutritional_status", "3. Current nutritional status :", "3. वर्तमान पोषण स्थिति:", listOf("SAM (WHZ < -3 or oedema)", "MAM (WHZ -3 to -2)", "Normal"), listOf("गंभीर कुपोषण SAM", "मध्यम कुपोषण MAM", "सामान्य")),
            FormField.Dropdown("outcome_since_last", "4. Outcome since last visit:", "4. पिछली मुलाक़ात के बाद परिणाम:", listOf("Relapse to SAM", "Remained SAM", "Improved to MAM", "Recovered (Normal)", "Died", "No change"), listOf("पुनः SAM", "SAM बना रहा", "MAM तक सुधार", "स्वस्थ (सामान्य)", "मृत्यु", "कोई बदलाव नहीं")),
            FormField.Text("change_date", "5. If change in nutritional status occurred, specify date:", "5. यदि पोषण स्थिति में बदलाव हुआ हो, तो तारीख़ बताएँ:"),
            
            FormField.Subheading("B. Re-treatment", "पुनः उपचार"),
            FormField.Dropdown("reenrolled", "1. Re-enrolled in CSAM after baseline:", "1. बेसलाइन के बाद CSAM में पुनः नामांकन:", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("reenrolled_when", "If yes, when?", "यदि हाँ, कब?"),
            FormField.Text("num_reenrolments", "2. Number of re-enrolments since baseline:", "2. बेसलाइन के बाद से पुनः नामांकनों की संख्या:", true),
            FormField.Dropdown("retreatment_status", "3. Current status of the re-treatment episode:", "3. वर्तमान पुनः उपचार की स्थिति:", listOf("Treatment completed", "Treatment ongoing", "Defaulted", "Died"), listOf("उपचार पूरा हो गया", "उपचार जारी है", "बीच में उपचार छोड़ दिया", "मृत्यु")),
            FormField.Dropdown("retreatment_outcome", "4. If completed, what was the outcome?", "4. यदि पूरा हो चुका है, तो उसका परिणाम क्या था?", listOf("Recovered", "Non-responder"), listOf("स्वस्थ", "सुधार नहीं हुआ")),

            FormField.Subheading("C. Programme Exposure & Service Contact", "कार्यक्रम संपर्क एवं सेवा संपर्क"),
            FormField.Dropdown("home_visits", "1. Since the last visit, did the child receive home visits?", "1. पिछली मुलाक़ात के बाद क्या घर मिलने आईं?", listOf("Regularly", "Occasionally", "No home visits received"), listOf("नियमित रूप से", "कभी-कभी", "कभी नहीं आईं")),
            FormField.Dropdown("counselling", "2. Was counselling on child feeding and nutrition provided?", "2. क्या खान-पान एवं पोषण के बारे में सलाह दी गई?", listOf("Yes, regularly", "Sometimes", "No"), listOf("हाँ, नियमित रूप से", "कभी-कभी", "नहीं")),
            FormField.Dropdown("referred_nrc", "3. Since the last visit, was the child referred to an NRC?", "3. क्या पिछली मुलाक़ात के बाद बच्चे को NRC में रेफर किया गया?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("referral_reason", "a) If yes, specify the reason:", "a) यदि हाँ, तो कारण बताएँ:"),
            FormField.Dropdown("referral_status", "b) What is the current status of the referral?", "b) रेफरल के बाद बच्चे की वर्तमान स्थिति क्या है?", listOf("Child returned home after treatment", "Child is currently admitted", "Child was not admitted despite referral", "Other"), listOf("उपचार के बाद घर लौट आया", "वर्तमान में भर्ती है", "भर्ती नहीं किया गया", "अन्य")),
            FormField.Dropdown("attended_followups", "4. Did the caregiver attend scheduled CSAM follow-up visits regularly?", "4. क्या नियमित रूप से भाग लिया?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("attended_no_reason", "If No, specify the reason:", "यदि नहीं, तो कारण बताएँ:")
        )),
        FormSection("SECTION 4: CHILD MORBIDITY PROFILE (LAST 2 WEEKS)", "खंड 4: बच्चे की बीमारी का विवरण (पिछले 2 सप्ताह)", listOf(
            FormField.Dropdown("fever", "1. Fever:", "1. बुखार:", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("fever_days", "Days:", "दिन:", true),
            FormField.Dropdown("diarrhoea", "2. Diarrhoea:", "2. दस्त:", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("diarrhoea_episodes", "Episodes:", "कितनी बार:", true),
            FormField.Dropdown("cough", "3. Cough with fast or difficult breathing?", "3. खाँसी के साथ तेज़ या कठिन साँस?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("cough_days", "Days:", "दिन:", true),
            FormField.Dropdown("other_illness", "4. Any other illness for which treatment was sought?", "4. कोई अन्य बीमारी, जिसके लिए इलाज कराया गया?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("other_illness_specify", "If Yes, specify:", "यदि हाँ, बताएँ:"),
            FormField.Dropdown("hospitalization", "5. Any hospitalization since last visit:", "5. पिछली मुलाक़ात के बाद कोई अस्पताल में भर्ती:", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("hosp_place", "If yes: Place:", "यदि हाँ: स्थान:"),
            FormField.Text("hosp_reason", "If yes: Reason:", "यदि हाँ: कारण:")
        )),
        FormSection("SECTION 5: Child Feeding Practices, Supplementary Nutrition, and Cultural Beliefs", "खंड 5: बाल आहार व्यवहार, पूरक पोषण एवं सांस्कृतिक मान्यताएँ", listOf(
            FormField.Subheading("A. Current Breastfeeding Status", "क. वर्तमान स्तनपान की स्थिति"),
            FormField.Dropdown("currently_breastfed", "1. Is the child currently being breastfed?", "1. क्या बच्चे को वर्तमान में स्तनपान कराया जा रहा है?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("bf_times_24h", "If YES: times in 24 hours:", "यदि हाँ: 24 घंटों में कितनी बार:", true),
            FormField.Text("stop_bf_age", "If NO: age when stopped (months):", "यदि नहीं: स्तनपान बंद करते समय आयु (महीने):", true),
            
            FormField.Subheading("B. Dietary Intake & Meal Pattern", "ख. आहार सेवन एवं भोजन की आवृत्ति"),
            FormField.CheckboxGroup("dietary_groups", "1. During the previous 24 hours, did the child consume any of the following food groups?", "1. पिछले 24 घंटों में बच्चे ने निम्नलिखित खाद्य समूहों में से कौन-कौन से खाद्य पदार्थ खाए थे?", listOf("Breastmilk", "Grains, roots and tubers", "Pulses/legumes/nuts", "Milk/milk products", "Flesh foods (meat/fish/chicken)", "Eggs", "Vitamin A-rich fruits/vegetables", "Other fruits/vegetables"), listOf("माँ का दूध", "अनाज, जड़ें एवं कंद", "दालें/फलियाँ/मेवे", "दूध एवं दुग्ध उत्पाद", "मांसाहार (मांस/मछली/चिकन)", "अंडे", "विटामिन ए युक्त फल एवं सब्ज़ियाँ", "अन्य फल एवं सब्ज़ियाँ")),
            FormField.Text("feeding_episodes", "2. Total number of feeding episodes yesterday (excluding breastfeeds):", "2. कल स्तनपान को छोड़कर कुल कितनी बार खिलाया गया?", true),
            
            FormField.Subheading("C. Supplementary Nutrition Programme (SNP) Utilization", "ग. पूरक पोषण कार्यक्रम (SNP) का उपयोग"),
            FormField.Dropdown("snp_provided", "1. Which supplementary nutrition was provided to the child?", "1. बच्चे को कौन-सा पूरक पोषण प्रदान किया गया?", listOf("Take-Home Ration (THR)", "Hot Cooked Meal (HCM)", "Both", "None"), listOf("टेक-होम राशन (THR)", "गर्म पका हुआ भोजन (HCM)", "दोनों", "कोई नहीं")),
            FormField.Dropdown("snp_regular", "2. Was the supplementary nutrition received regularly?", "2. क्या बच्चे को पूरक पोषण नियमित रूप से प्राप्त हुआ?", listOf("Regularly", "Irregularly", "Not received"), listOf("नियमित रूप से", "अनियमित रूप से", "प्राप्त नहीं हुआ")),
            FormField.Text("snp_days", "3. During the last 7 days, on how many days did the child consume the supplementary nutrition provided?", "3. पिछले 7 दिनों में बच्चे ने प्रदान किया गया पूरक पोषण कितने दिनों तक सेवन किया?", true),
            FormField.Dropdown("snp_type", "4. What type of supplementary nutrition did the child usually consume?", "4. बच्चा सामान्यतः किस प्रकार का पूरक पोषण सेवन करता था?", listOf("THR (Balahar/Khichdi/Halwa/Other)", "HCM (Hot cooked meal at AWC)"), listOf("THR (बाल आहार/खिचड़ी/हलवा/अन्य)", "HCM (आँगनवाड़ी केंद्र पर परोसा गया गर्म पका हुआ भोजन)")),
            FormField.Dropdown("snp_consumed", "5. When supplementary nutrition was provided, how much of the serving did the child usually consume?", "5. जब पूरक पोषण दिया गया, तो बच्चा सामान्यतः उसकी कितनी मात्रा खाता था?", listOf("All or almost all", "About half", "Only a small amount", "Did not consume"), listOf("पूरा या लगभग पूरा", "लगभग आधा", "थोड़ी मात्रा", "बिल्कुल नहीं खाया")),
            FormField.Dropdown("snp_not_consumed_reason", "6. If partially/not consumed, what was the main reason?", "6. यदि पूरक पोषण आंशिक रूप से या बिल्कुल भी सेवन नहीं किया गया, तो उसका मुख्य कारण क्या था?", listOf("Child refused", "Shared with family", "Not received regularly", "Poor taste", "Caregiver unaware of preparation", "Child did not attend Anganwadi", "Other"), listOf("बच्चे ने खाने से मना कर दिया", "परिवार के अन्य सदस्यों के साथ साझा किया गया", "नियमित रूप से प्राप्त नहीं हुआ", "स्वाद अच्छा नहीं लगा", "देखभालकर्ता को तैयार/उपयोग करने की जानकारी नहीं थी", "बच्चा आँगनवाड़ी केंद्र नहीं गया", "अन्य")),

            FormField.Subheading("D. Feeding Behavior (Responsive Feeding)", "घ. खिलाने का व्यवहार"),
            FormField.Dropdown("who_feeds", "1. Who usually feeds the child?", "1. बच्चे को आमतौर पर कौन खिलाता है?", listOf("Child eats independently", "Mother feeds", "Other caregiver feeds", "Mixed"), listOf("बच्चा स्वयं खाता है", "माता खिलाती है", "अन्य देखभालकर्ता", "मिश्रित")),
            FormField.Dropdown("child_refuse", "2. Does the child refuse food during feeding?", "2. क्या बच्चा खिलाते समय खाने से मना करता है?", listOf("No", "Occasionally", "Frequently"), listOf("नहीं", "कभी-कभार", "अक्सर")),
            FormField.Dropdown("difficulty_feeding", "3. Any difficulty during feeding?", "3. खिलाते समय कोई कठिनाई?", listOf("No", "Yes"), listOf("नहीं", "हाँ")),
            FormField.CheckboxGroup("difficulty_type", "If YES, what kind of difficulty?", "यदि हाँ, किस तरह की कठिनाई?", listOf("Vomiting during/after feeding", "Difficulty swallowing", "Coughing/choking while eating", "Child spits out food", "Child becomes irritable/cries", "Illness affecting feeding", "Other"), listOf("खिलाते/बाद में उल्टी", "निगलने में कठिनाई", "खाते समय खाँसी/अटकना", "बच्चा खाना थूकता है", "बच्चा चिड़चिड़ा/रोता है", "बीमारी से खाने पर असर", "अन्य")),

            FormField.Subheading("E. Food Restrictions & Cultural Beliefs", "ङ खान-पान पर रोक एवं सांस्कृतिक मान्यताएँ"),
            FormField.Dropdown("food_restricted", "1. Are any foods restricted/avoided for the child?", "1. क्या बच्चे के लिए किसी भोजन पर रोक/परहेज़ है?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.CheckboxGroup("foods_avoided", "2. If Yes, which foods are avoided?", "2. यदि हाँ, किन खाद्यों से परहेज़ है?", listOf("Egg", "Meat / Chicken", "Fish", "Milk / Milk products", "Fruits", "Pulses", "Green leafy vegetables", "Other"), listOf("अंडा", "मांस / चिकन", "मछली", "दूध / दुग्ध उत्पाद", "फल", "दालें", "हरी पत्तेदार सब्ज़ियाँ", "अन्य")),
            FormField.Dropdown("restriction_reason", "3. Reason for restriction/avoidance:", "3. रोक/परहेज़ का कारण:", listOf("Cultural / Traditional belief", "Religious belief", "Child illness", "digestion concern", "Economic constraint", "Other"), listOf("सांस्कृतिक/पारंपरिक मान्यता", "धार्मिक मान्यता", "बच्चे की बीमारी", "पाचन की चिंता", "आर्थिक तंगी", "अन्य"))
        )),
        FormSection("SECTION 6: HEALTH-SEEKING BEHAVIOUR", "खंड 6: इलाज ढूँढने का व्यवहार", listOf(
            FormField.Dropdown("illness_experience", "1. Since the last follow-up visit, did the child experience any illness requiring medical care?", "1. क्या पिछली फ़ॉलो-अप मुलाक़ात के बाद बच्चे को ऐसी कोई बीमारी हुई जिसके लिए उपचार की आवश्यकता पड़ी?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Dropdown("where_care", "2. During the child's most recent illness, where was treatment first sought?", "2. पिछली फ़ॉलो-अप मुलाक़ात के बाद बच्चे की सबसे हाल की बीमारी में सबसे पहले उपचार कहाँ कराया गया?", listOf("Government health facility", "Private healthcare provider", "AYUSH practitioner", "Traditional healer", "No treatment sought"), listOf("सरकारी स्वास्थ्य संस्थान", "निजी चिकित्सक/अस्पताल", "आयुष चिकित्सक", "पारंपरिक चिकित्सक/झोलाछाप", "कोई उपचार नहीं कराया")),
            FormField.Dropdown("time_care", "3. How soon after the illness began was treatment sought?", "3. बीमारी शुरू होने के बाद उपचार कराने में कितना समय लगा?", listOf("Same day", "Within 1–2 days", "After more than 2 days"), listOf("उसी दिन", "1–2 दिनों के भीतर", "2 दिनों से अधिक बाद")),
            FormField.CheckboxGroup("care_barriers", "4. What were the main barriers to seeking treatment?", "4. उपचार प्राप्त करने में मुख्य बाधाएँ क्या थीं?", listOf("Cost", "Distance", "Lack of transport", "Lack of awareness", "Family decision", "Other"), listOf("खर्च", "दूरी", "परिवहन की सुविधा का अभाव", "जानकारी का अभाव", "परिवार का निर्णय", "अन्य"))
        )),
        FormSection("SECTION 7: HOUSEHOLD ENVIRONMENT", "खंड 7: घरेलू वातावरण", listOf(
            FormField.Dropdown("drinking_water", "1. Main source of drinking water:", "1. पीने के पानी का स्रोत:", listOf("Piped water", "Borewell", "well", "Other"), listOf("पाइप का पानी", "बोरवेल", "कुआं", "अन्य")),
            FormField.Dropdown("toilet", "2. Toilet facility:", "2. शौचालय सुविधा:", listOf("Private", "Public", "Open defecation"), listOf("निजी", "सार्वजनिक", "खुले में शौच"))
        ))
    )
}
"""

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "r") as f:
    orig = f.read()

import re
res = re.sub(r'    val followupSections = listOf\(.*?\n\}', content, orig, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "w") as f:
    f.write(res)
