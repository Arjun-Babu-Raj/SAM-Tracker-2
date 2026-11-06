import re

content = """package com.example.ui

sealed class FormField(val id: String, val labelEn: String, val labelHi: String) {
    class Text(id: String, labelEn: String, labelHi: String, val isNumber: Boolean = false) : FormField(id, labelEn, labelHi)
    class Dropdown(id: String, labelEn: String, labelHi: String, val optionsEn: List<String>, val optionsHi: List<String>) : FormField(id, labelEn, labelHi)
    class Subheading(labelEn: String, labelHi: String) : FormField("", labelEn, labelHi)
}

data class FormSection(val titleEn: String, val titleHi: String, val fields: List<FormField>)

object QuestionBank {
    val baselineSections = listOf(
        FormSection("SECTION 1: IDENTIFICATION", "खंड 1: पहचान", listOf(
            FormField.Text("study_id", "1. Study ID:", "1. अध्ययन क्रमांक :"),
            FormField.Text("csam_id", "2. CSAM ID (SAMPARK):", "2. CSAM क्रमांक (संपर्क/SAMPARK):"),
            FormField.Text("awc_name", "3. Anganwadi Centre (AWC) Name:", "3. आँगनवाड़ी केंद्र (AWC) का नाम:"),
            FormField.Text("block_district", "4. Block / District:", "4. ब्लॉक / ज़िला:"),
            FormField.Dropdown("total_enrolments", "5. Total number of SAM/MAM treatment enrolments including current episode:", "5. इस बार सहित, बच्चे का SAM/MAM उपचार में कुल कितनी बार नामांकन हुआ:", listOf("1", "2", ">=3", "Don't know"), listOf("1", "2", ">=3", "पता नहीं")),
            FormField.Text("date_enrolment", "6. Date of enrolment into the current CSAM treatment episode:", "6. वर्तमान CSAM उपचार चक्र में नामांकन की तिथि:"),
            FormField.Dropdown("month_enrolment", "7. Month of CSAM enrolment:", "7. CSAM नामांकन का महीना:", listOf("Month 1", "Month 2", "Month 3"), listOf("महीना 1", "महीना 2", "महीना 3")),
            FormField.Dropdown("baseline_timing", "8. Baseline timing category:", "8. बेसलाइन समय श्रेणी:", listOf("Month 4", "Month 5", "Month 6"), listOf("महीना 4", "महीना 5", "महीना 6")),
            FormField.Text("date_baseline", "9. Date of Baseline Assessment:", "9. बेसलाइन आकलन की तारीख़:"),
            FormField.Dropdown("season", "10. Current season at time of baseline:", "10. बेसलाइन के समय का मौसम:", listOf("Summer", "Monsoon", "Winter"), listOf("गर्मी", "बरसात", "सर्दी")),
            FormField.Dropdown("consent", "11. Informed consent obtained:", "11. सूचित सहमति ली गई:", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("data_collector", "12. Data collector name :", "12. आँकड़ा संग्राहक का नाम :")
        )),

        FormSection("SECTION 2: SOCIODEMOGRAPHIC CHARACTERISTICS", "खंड 2: सामाजिक-जनसांख्यिकीय विवरण", listOf(
            FormField.Subheading("A. Child Details", "क. बच्चे का विवरण"),
            FormField.Text("child_name", "1. Child name:", "1. बच्चे का नाम:"),
            FormField.Text("child_age_months", "2. Age (in completed months):", "2. आयु (पूरे महीनों में):", true),
            FormField.Dropdown("age_category", "3. Age category:", "3. आयु वर्ग:", listOf("6–23 months", "24–59 months"), listOf("6-23 महीने", "24-59 महीने")),
            FormField.Dropdown("sex", "4. Sex:", "4. लिंग:", listOf("Male", "Female"), listOf("पुरुष", "स्त्री")),
            FormField.Text("num_siblings", "5. Number of siblings:", "5. भाई-बहनों की संख्या:", true),
            
            FormField.Subheading("B. Primary Caregiver (Childcare Responsibility)", "ख. मुख्य देखभालकर्ता (बच्चे की देखभाल की ज़िम्मेदारी)"),
            FormField.Dropdown("primary_caregiver", "1. Primary caregiver:", "1. मुख्य देखभालकर्ता का विवरण", listOf("Mother", "Father", "Grandmother", "Grandfather", "Other"), listOf("माता", "पिता", "दादी / नानी", "दादा / नाना", "अन्य")),
            FormField.Text("caregiver_age", "2. Age (years):", "2. आयु (वर्ष):", true),
            FormField.Dropdown("caregiver_education", "3. Education:", "3. शैक्षिक योग्यता:", listOf("No schooling", "Primary", "Secondary", "Higher Secondary", "Graduate & above"), listOf("कोई औपचारिक शिक्षा नहीं", "प्राथमिक", "माध्यमिक", "उच्च माध्यमिक", "स्नातक एवं उससे अधिक")),
            FormField.Dropdown("caregiver_occupation", "4. Occupation:", "4. व्यवसाय:", listOf("Homemaker", "Labour", "Agriculture", "Service", "Business", "Other"), listOf("गृहिणी / गृहकार्य", "दिहाड़ी मज़दूरी", "कृषि", "नौकरी (सरकारी/निजी)", "व्यवसाय", "अन्य")),
            FormField.Dropdown("caregiver_health", "5. Does the primary caregiver have any long-term health condition that affects their ability to care for the child?", "5. क्या मुख्य देखभालकर्ता को कोई ऐसी दीर्घकालिक स्वास्थ्य समस्या है जो बच्चे की देखभाल करने की उनकी क्षमता को प्रभावित करती है?", listOf("No", "Yes", "Don't know"), listOf("नहीं", "हाँ", "पता नहीं")),
            FormField.Text("caregiver_health_specify", "If Yes (specify):", "यदि हाँ (कृपया बताएँ):"),
            FormField.Dropdown("decides_feeding", "6. Who mainly decides about child feeding?", "6. बच्चे के खान-पान का निर्णय मुख्यत: कौन लेता है?", listOf("Mother", "Father", "Grandparent", "Joint decision"), listOf("माता", "पिता", "दादा-दादी/नाना-नानी", "संयुक्त निर्णय")),

            FormField.Subheading("C. Household Socioeconomic Status", "ग. परिवार की सामाजिक-आर्थिक स्थिति"),
            FormField.Dropdown("family_type", "1. Family type:", "1. परिवार का प्रकार:", listOf("Nuclear", "Joint", "Three-generation / Extended family"), listOf("एकल", "संयुक्त", "तीन-पीढ़ी / विस्तृत परिवार")),
            FormField.Text("total_members", "2. Total family members:", "2. परिवार के कुल सदस्य:", true),
            FormField.Dropdown("social_category", "3. Social category:", "3. सामाजिक वर्ग:", listOf("SC", "ST", "OBC", "General", "Don't know"), listOf("अनुसूचित जाति", "अनुसूचित जनजाति", "अन्य पिछड़ा वर्ग", "सामान्य", "पता नहीं")),
            FormField.Dropdown("religion", "4. Religion:", "4. धर्म:", listOf("Hindu", "Muslim", "Christian", "Sikh", "Buddhist", "Other"), listOf("हिंदू", "मुस्लिम", "ईसाई", "सिख", "बौद्ध", "अन्य")),
            FormField.Text("monthly_income", "5. Approximate total monthly household income:", "5. परिवार के सभी कमाने वाले सदस्यों की अनुमानित कुल मासिक आय:", true),
            FormField.Dropdown("ration_card", "6. Ration card type:", "6. राशन कार्ड का प्रकार:", listOf("APL", "BPL", "Antyodaya (AAY)", "None"), listOf("APL", "BPL", "अंत्योदय (AAY)", "कोई नहीं")),
            FormField.Dropdown("migration_status", "7. Migration status:", "7. पलायन की स्थिति:", listOf("No migration", "Seasonal migration", "Permanent migration"), listOf("पलायन नहीं", "मौसमी पलायन", "स्थायी पलायन")),
            FormField.Text("migration_months", "If Seasonal migration (months/year):", "यदि मौसमी पलायन (महीने/वर्ष):")
        )),

        FormSection("SECTION 3: MATERNAL & EARLY LIFE FACTORS", "खंड 3: मातृ एवं प्रारंभिक जीवन कारक -", listOf(
            FormField.Dropdown("place_delivery", "1. Place of delivery:", "1. प्रसव का स्थान:", listOf("Home", "Government facility", "Private facility"), listOf("घर", "सरकारी अस्पताल", "निजी अस्पताल")),
            FormField.Text("birth_weight", "2. Birth weight (kg):", "2. जन्म के समय वज़न:", true),
            FormField.Dropdown("preterm_birth", "3. Preterm birth:", "3. समय से पहले जन्म:", listOf("Yes", "No", "Don't know"), listOf("हाँ", "नहीं", "पता नहीं")),
            FormField.Dropdown("anc_visits", "4. Number of ANC visits during pregnancy:", "4. गर्भावस्था में प्रसवपूर्व जाँच (ANC) की संख्या:", listOf("0-3", ">=4", "Don't know"), listOf("0-3", ">=4", "पता नहीं"))
        )),

        FormSection("SECTION 4: CSAM TREATMENT & PROGRAMME EXPOSURE", "खंड 4: CSAM उपचार एवं कार्यक्रम संपर्क", listOf(
            FormField.Subheading("A. Anthropometric & Clinical Status", "क. शारीरिक माप एवं चिकित्सीय स्थिति"),
            FormField.Text("weight_enrolment", "Weight (kg) At enrolment (1st week):", "वज़न (कि.ग्रा.) नामांकन के समय (1st week):", true),
            FormField.Text("weight_12weeks", "Weight (kg) At 12 weeks:", "वज़न (कि.ग्रा.) 12 सप्ताह पर:", true),
            FormField.Text("height_enrolment", "Height/Length (cm) At enrolment (1st week):", "ऊँचाई/लंबाई (से.मी.) नामांकन के समय (1st week):", true),
            FormField.Text("height_12weeks", "Height/Length (cm) At 12 weeks:", "ऊँचाई/लंबाई (से.मी.) 12 सप्ताह पर:", true),
            FormField.Text("whz_enrolment", "WHZ At enrolment (1st week):", "WHZ नामांकन के समय (1st week):", true),
            FormField.Text("whz_12weeks", "WHZ At 12 weeks:", "WHZ 12 सप्ताह पर:", true),

            FormField.Dropdown("growth_trend", "1. Growth trend during the initial 12-week CSAM treatment period:", "1. प्रारंभिक 12-सप्ताह की CSAM उपचार अवधि के दौरान वृद्धि का रुझान:", listOf("Consistent weight gain", "Fluctuating weight", "No significant improvement", "Weight loss"), listOf("लगातार वज़न में वृद्धि", "वज़न में उतार-चढ़ाव", "कोई उल्लेखनीय सुधार नहीं", "वज़न में कमी")),
            FormField.Dropdown("oedema", "2. Bilateral pitting oedema:", "2. दोनों पैरों में दबाने पर गड्ढा (सूजन):", listOf("Present", "Absent"), listOf("है", "नहीं")),
            FormField.Dropdown("appetite_test", "3. Appetite test result:", "3. भूख परीक्षण परिणाम:", listOf("Passed", "Failed"), listOf("उत्तीर्ण", "अनुत्तीर्ण")),
            FormField.Dropdown("nutritional_status_baseline", "4. Nutritional status at baseline:", "4. बेसलाइन पर पोषण स्थिति:", listOf("SAM (WHZ < -3 or oedema)", "MAM (WHZ -3 to -2)", "Normal (WHZ >= -2)"), listOf("गंभीर कुपोषण SAM (WHZ < -3 या सूजन)", "मध्यम कुपोषण MAM (WHZ -3 से -2)", "सामान्य (WHZ >= -2)")),
            FormField.Dropdown("programme_outcome", "5. Programme outcome (key variable):", "5. कार्यक्रम परिणाम (मुख्य चर):", listOf("Recovered", "Non-responder", "Defaulter", "Died"), listOf("स्वस्थ हुआ", "सुधार नहीं हुआ", "बीच में छोड़ा", "मृत्यु")),

            FormField.Subheading("B. Programme Exposure & Service Contact", "ख. कार्यक्रम संपर्क एवं सेवा संपर्क"),
            FormField.Dropdown("home_visits", "1. During the 12-week CSAM treatment period, did the child receive home visits from AWW and/or ASHA?", "1. क्या 12-सप्ताह की CSAM उपचार अवधि के दौरान AWW और/या ASHA कार्यकर्ता बच्चे के घर मिलने आईं?", listOf("Regularly", "Occasionally", "No home visits received"), listOf("नियमित रूप से", "कभी-कभी", "कभी नहीं आईं")),
            FormField.Dropdown("counselling", "2. Was counselling on child feeding/nutrition provided during treatment visits?", "2. क्या उपचार के दौरान बच्चे के खान-पान/पोषण पर सलाह दी गई?", listOf("Yes regularly", "Sometimes", "No"), listOf("हाँ नियमित", "कभी-कभी", "नहीं")),
            FormField.Dropdown("referred_nrc", "3. Was the child referred to NRC / higher facility during the initial CSAM treatment cycle?", "3. क्या शुरुआती CSAM उपचार चक्र में बच्चे को NRC / बड़े अस्पताल रेफर किया गया?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("referred_reason", "If yes, specify reason:", "यदि हाँ, कारण बताएँ:"),
            FormField.Dropdown("attended_visits", "4. Did the caregiver attend scheduled CSAM follow-up/weighing visits regularly during the 12-week treatment period?", "4. क्या देखभालकर्ता ने 12-सप्ताह उपचार अवधि में निर्धारित CSAM फ़ॉलो-अप/वज़न दौरों में नियमित रूप से भाग लिया?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("attended_no_reason", "If No, reasons:", "यदि नहीं, कारण:")
        )),

        FormSection("SECTION 5: CHILD MORBIDITY PROFILE (LAST 2 WEEKS)", "खंड 5: बच्चे की बीमारी का विवरण (पिछले 2 सप्ताह)", listOf(
            FormField.Dropdown("fever", "1. Fever:", "1. बुखार:", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("fever_days", "Fever Days:", "बुखार दिन:", true),
            FormField.Dropdown("diarrhoea", "2. Diarrhoea:", "2. दस्त:", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("diarrhoea_episodes", "Diarrhoea Episodes:", "दस्त कितनी बार:", true),
            FormField.Dropdown("cough", "3. Did the child have cough with fast or difficult breathing?", "3. बच्चे को खाँसी के साथ तेज़ या कठिन साँस लेने की समस्या हुई थी?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("cough_days", "Cough Days:", "खाँसी दिन:", true),
            FormField.Dropdown("other_illness", "4. Did the child have any other illness for which treatment was sought?", "4. बच्चे को कोई अन्य बीमारी हुई, जिसके लिए उपचार/इलाज कराया गया?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("other_illness_specify", "If Yes, specify:", "यदि हाँ, बताएँ:"),
            FormField.Dropdown("hospitalization", "5. Any hospitalization during treatment:", "5. उपचार के दौरान कोई अस्पताल में भर्ती:", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("hosp_place", "If yes: Place:", "यदि हाँ: स्थान:"),
            FormField.Text("hosp_reason", "If yes: Reason:", "यदि हाँ: कारण:"),
            FormField.Dropdown("immunized", "6. completely immunized for age:", "6. आयु अनुसार पूर्ण टीकाकरण:", listOf("Yes", "No", "Unknown"), listOf("हाँ", "नहीं", "पता नहीं"))
        )),

        FormSection("SECTION 6: Child Feeding Practices, Supplementary Nutrition, and Cultural Beliefs", "खंड 6: बाल आहार व्यवहार, पूरक पोषण एवं सांस्कृतिक मान्यताएँ", listOf(
            FormField.Subheading("A. Breastfeeding & feeding history", "क. स्तनपान एवं आहार का इतिहास"),
            FormField.Dropdown("food_consumed", "1. Main type of food currently consumed by the child:", "1. वर्तमान में बच्चा मुख्यतः क्या खाता है:", listOf("Breastfeeding", "Complementary/home-cooked food", "THR (Take-Home Ration)", "Other"), listOf("माँ का दूध", "ऊपरी/घर का बना भोजन", "THR (घर ले जाने वाला राशन)", "अन्य")),
            FormField.Dropdown("breastfeeding_init", "2. How soon after birth was breastfeeding initiated?", "2. जन्म के कितनी देर बाद स्तनपान शुरू किया गया?", listOf("Within 1 hour of birth", "After 1 hour of birth", "Not breastfed"), listOf("जन्म के 1 घंटे के भीतर", "1 घंटे के बाद", "स्तनपान नहीं कराया")),
            FormField.Dropdown("exclusively_breastfed", "3. Was the child exclusively breastfed for the first 6 months of life?", "3. क्या बच्चे को पहले 6 महीने केवल माँ का दूध ही दिया गया?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("exclusive_bf_months", "If No: For how many completed months was exclusive breastfeeding followed?", "यदि नहीं: कितने पूरे महीनों तक केवल स्तनपान कराया गया?", true),
            FormField.Dropdown("intro_before_6m", "If No: What was introduced before 6 months?", "यदि नहीं: महीने से पहले क्या शुरू किया गया?", listOf("Water", "Animal milk", "Formula milk", "Honey", "Semi-solid/solid food", "Other"), listOf("पानी", "पशु का दूध", "डिब्बे का दूध", "शहद", "अर्ध-ठोस/ठोस भोजन", "अन्य")),
            FormField.Dropdown("currently_breastfed", "4. Is the child currently being breastfed?", "4. क्या बच्चे को अभी स्तनपान कराया जा रहा है?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Text("bf_times_24h", "If YES: How many times was the child breastfed in the last 24 hours?", "यदि हाँ: पिछले 24 घंटों में बच्चे को कितनी बार स्तनपान कराया?", true),
            FormField.Text("stop_bf_age", "If NO: Age at stopping breastfeeding (months):", "यदि नहीं: स्तनपान बंद करने के समय बच्चे की आयु (महीने):", true),
            FormField.Text("comp_feeding_age", "5. Age of complementary feeding initiation (months):", "5. ऊपरी आहार शुरू करने के समय की आयु (महीने):", true),

            FormField.Subheading("B. Dietary intake & Meal Pattern", "ख. आहार सेवन एवं भोजन का तरीका"),
            FormField.Dropdown("dietary_groups", "1. During the previous 24 hours, did the child consume any of the following food groups?", "1. पिछले 24 घंटों में बच्चे ने निम्नलिखित खाद्य समूहों में से कौन-कौन से खाद्य पदार्थ खाए थे?", listOf("Breastmilk", "Grains, roots, tubers", "Pulses/legumes/nuts", "Milk/milk products", "Flesh foods", "Eggs", "Vitamin A-rich fruits/vegetables", "Other fruits/vegetables"), listOf("माँ का दूध", "अनाज, जड़ें, कंद", "दालें/फलियाँ/मेवे", "दूध/दुग्ध उत्पाद", "माँसाहार", "अंडे", "विटामिन ए युक्त फल/सब्ज़ी", "अन्य फल/सब्ज़ी")),
            FormField.Text("feeding_episodes", "2. Total number of feeding episodes yesterday (excluding breastfeeds):", "2. कल (पिछले 24 घंटों) बच्चे को स्तनपान को छोड़कर कुल कितनी बार खिलाया गया?", true),

            FormField.Subheading("C. Supplementary Nutrition Programme (SNP) Utilization", "ग. पूरक पोषण कार्यक्रम (SNP) का उपयोग"),
            FormField.Dropdown("snp_provided", "1. During the current CSAM treatment period, which supplementary nutrition was provided?", "1. उपचार अवधि के दौरान बच्चे को कौन-सा पूरक पोषण प्रदान किया गया?", listOf("Take-Home Ration (THR)", "Hot Cooked Meal (HCM)", "Both", "None"), listOf("टेक-होम राशन (THR)", "गर्म पका हुआ भोजन (HCM)", "दोनों", "कोई नहीं")),
            FormField.Dropdown("snp_regular", "2. Was the supplementary nutrition received regularly?", "2. क्या बच्चे को पूरक पोषण नियमित रूप से प्राप्त हुआ?", listOf("Regularly", "Irregularly", "Not received"), listOf("नियमित रूप से", "अनियमित रूप से", "प्राप्त नहीं हुआ")),
            FormField.Text("snp_days", "3. During the last 7 days, on how many days did the child consume the supplementary nutrition provided?", "3. पिछले 7 दिनों में बच्चे ने प्रदान किया गया पूरक पोषण कितने दिनों तक सेवन किया?", true),
            FormField.Dropdown("snp_type", "4. What type of supplementary nutrition did the child usually consume?", "4. बच्चा सामान्यतः किस प्रकार का पूरक पोषण सेवन करता था?", listOf("THR (Balahar/Khichdi/Halwa/Other)", "HCM (Hot cooked meal at AWC)"), listOf("THR (बाल आहार/खिचड़ी/हलवा/अन्य)", "HCM (आँगनवाड़ी केंद्र पर परोसा गया गर्म पका हुआ भोजन)")),
            FormField.Dropdown("snp_consumed", "5. When supplementary nutrition was provided, how much of the serving did the child usually consume?", "5. जब पूरक पोषण दिया गया, तो बच्चा सामान्यतः उसकी कितनी मात्रा खाता था?", listOf("All or almost all", "About half", "Only a small amount", "Did not consume"), listOf("पूरा या लगभग पूरा", "लगभग आधा", "थोड़ी मात्रा", "बिल्कुल नहीं खाया")),
            FormField.Dropdown("snp_not_consumed_reason", "6. If partially/not consumed, what was the main reason?", "6. यदि पूरक पोषण आंशिक रूप से या बिल्कुल भी सेवन नहीं किया गया, तो उसका मुख्य कारण क्या था?", listOf("Child refused", "Shared with family", "Not received regularly", "Poor taste", "Caregiver unaware of preparation", "Child did not attend Anganwadi", "Other"), listOf("बच्चे ने खाने से मना कर दिया", "परिवार के अन्य सदस्यों के साथ साझा किया गया", "नियमित रूप से प्राप्त नहीं हुआ", "स्वाद अच्छा नहीं लगा", "देखभालकर्ता को तैयार/उपयोग करने की जानकारी नहीं थी", "बच्चा आँगनवाड़ी केंद्र नहीं गया", "अन्य")),

            FormField.Subheading("D. Feeding Behavior (Responsive Feeding)", "घ. खिलाने का व्यवहार (संवेदनशील आहार)"),
            FormField.Dropdown("who_feeds", "1. Who usually feeds the child?", "1. बच्चे को आमतौर पर कौन खिलाता है?", listOf("Child eats independently", "Mother feeds", "Other caregiver feeds", "Mixed"), listOf("बच्चा स्वयं खाता है", "माता खिलाती है", "अन्य देखभालकर्ता", "मिश्रित")),
            FormField.Dropdown("child_refuse", "2. Does the child refuse food during feeding?", "2. क्या बच्चा खिलाते समय खाने से मना करता है?", listOf("No", "Occasionally", "Frequently"), listOf("नहीं", "कभी-कभार", "अक्सर")),
            FormField.Dropdown("difficulty_feeding", "3. Any difficulty during feeding?", "3. खिलाते समय कोई कठिनाई?", listOf("No", "Yes"), listOf("नहीं", "हाँ")),
            FormField.Dropdown("difficulty_type", "If YES, what kind of difficulty?", "यदि हाँ, किस तरह की कठिनाई?", listOf("Vomiting during/after feeding", "Difficulty swallowing", "Coughing/choking while eating", "Child spits out food", "Child becomes irritable/cries", "Illness affecting feeding", "Other"), listOf("खिलाते/बाद में उल्टी", "निगलने में कठिनाई", "खाते समय खाँसी/अटकना", "बच्चा खाना थूकता है", "बच्चा चिड़चिड़ा/रोता है", "बीमारी से खाने पर असर", "अन्य")),

            FormField.Subheading("E. Food Restrictions & Cultural Beliefs", "ङ खान-पान पर रोक एवं सांस्कृतिक मान्यताएँ"),
            FormField.Dropdown("food_restricted", "1. Are any foods restricted/avoided for the child?", "1. क्या बच्चे के लिए किसी भोजन पर रोक/परहेज़ है?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Dropdown("foods_avoided", "2. If Yes, which foods are avoided?", "2. यदि हाँ, किन खाद्यों से परहेज़ है?", listOf("Egg", "Meat / Chicken", "Fish", "Milk / Milk products", "Fruits", "Pulses", "Green leafy vegetables", "Other"), listOf("अंडा", "मांस / चिकन", "मछली", "दूध / दुग्ध उत्पाद", "फल", "दालें", "हरी पत्तेदार सब्ज़ियाँ", "अन्य")),
            FormField.Dropdown("restriction_reason", "3. Reason for restriction/avoidance:", "3. रोक/परहेज़ का कारण:", listOf("Cultural / Traditional belief", "Religious belief", "Child illness", "digestion concern", "Economic constraint", "Other"), listOf("सांस्कृतिक/पारंपरिक मान्यता", "धार्मिक मान्यता", "बच्चे की बीमारी", "पाचन की चिंता", "आर्थिक तंगी", "अन्य"))
        )),

        FormSection("SECTION 7: HEALTH-SEEKING BEHAVIOUR", "खंड 7: इलाज ढूँढने का व्यवहार", listOf(
            FormField.Dropdown("where_care", "1. During the child’s most recent illness, where was care first sought?", "1. बच्चे की सबसे हाल की बीमारी में सबसे पहले इलाज कहाँ कराया?", listOf("Government facility", "Private provider", "AYUSH provider", "Traditional healer", "No care sought"), listOf("सरकारी अस्पताल", "निजी डॉक्टर", "आयुष चिकित्सक", "पारंपरिक/झोलाछाप", "कोई इलाज नहीं")),
            FormField.Dropdown("time_care", "2. Time taken to seek care after illness onset:", "2. बीमारी शुरू होने के बाद इलाज कराने में लगा समय:", listOf("Same day", "1–2 days", ">2 days"), listOf("उसी दिन", "1-2 दिन", ">2 दिन")),
            FormField.Dropdown("care_barriers", "3. What were the main barriers to seeking care?", "3. इलाज तक पहुँचने में बाधाएँ:", listOf("Cost", "Distance", "Lack of transport", "Lack of Awareness", "Family decision", "Other"), listOf("खर्च", "दूरी", "यातायात की कमी", "जानकारी की कमी", "पारिवारिक निर्णय", "अन्य"))
        )),

        FormSection("SECTION 8: HOUSEHOLD ENVIRONMENT", "खंड 8: घरेलू वातावरण", listOf(
            FormField.Dropdown("drinking_water", "1. Main source of drinking water:", "1. पीने के पानी का स्रोत:", listOf("Piped water", "Borewell", "well", "Other"), listOf("पाइप का पानी", "बोरवेल", "कुआं", "अन्य")),
            FormField.Dropdown("toilet", "2. Toilet facility:", "2. शौचालय सुविधा:", listOf("Private", "Public", "Open defecation"), listOf("निजी", "सार्वजनिक", "खुले में शौच"))
        )),

        FormSection("SECTION 9: CAREGIVER KNOWLEDGE", "खंड 9: देखभालकर्ता की जानकारी", listOf(
            FormField.Dropdown("heard_sam", "1. Have you ever heard of severe acute malnutrition (SAM) or severe weakness/low weight in children?", "1. क्या आपने बच्चों में गंभीर तीव्र कुपोषण (SAM) या अत्यधिक कमज़ोरी/कम वज़न के बारे में सुना है?", listOf("Yes", "No"), listOf("हाँ", "नहीं")),
            FormField.Dropdown("sam_signs", "If YES, what are the signs of severe malnutrition in a child?", "यदि हाँ, बच्चे में गंभीर कुपोषण के लक्षण क्या हैं?", listOf("Very thin child", "Swelling of feet/face", "Poor appetite", "Frequent illness", "Don't know / no correct response"), listOf("बहुत दुबला बच्चा", "पैर/चेहरे में सूजन", "कम भूख", "बार-बार बीमारी", "पता नहीं / कोई सही उत्तर नहीं")),
            FormField.Dropdown("heard_csam", "2. Have you heard about the CSAM programme/services for treating malnourished children?", "2. क्या आपने कुपोषित बच्चों के इलाज हेतु CSAM कार्यक्रम/सेवाओं के बारे में सुना है?", listOf("Yes", "No"), listOf("हाँ", "नहीं"))
        ))
    )
}
"""

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "w") as f:
    f.write(content)

