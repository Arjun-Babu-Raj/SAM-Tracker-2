sed -i 's/fun BaselineScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {/fun BaselineScreen(studyId: String? = null, viewModel: MainViewModel, onNavigateBack: () -> Unit) {/' app/src/main/java/com/example/ui/BaselineScreen.kt

sed -i '/val formState by viewModel.baselineState.collectAsState()/a \
    LaunchedEffect(studyId) {\
        if (!studyId.isNullOrEmpty()) {\
            val childData = viewModel.getChildProfileDataOnce(studyId)\
            if (childData != null) {\
                val (child, baseline, _) = childData\
                if (baseline != null) {\
                    try {\
                        val json = JSONObject(baseline.extraDataJson)\
                        val map = mutableMapOf<String, String>()\
                        json.keys().forEach { key -> map[key] = json.getString(key) }\
                        dynamicAnswers = map\
                    } catch (e: Exception) { e.printStackTrace() }\
                }\
            }\
        }\
    }' app/src/main/java/com/example/ui/BaselineScreen.kt
