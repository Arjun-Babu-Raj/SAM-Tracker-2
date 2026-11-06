sed -i '/fun getChildProfileData(/i \
    suspend fun getChildProfileDataOnce(studyId: String): Triple<Child, BaselineAssessment?, List<LongitudinalFollowup>>? {\
        val child = repository.searchChild(studyId)\
        if (child != null) {\
            val baselines = repository.getAllBaselines().filter { it.studyId == studyId }\
            val followups = repository.getAllFollowups().filter { it.studyId == studyId }\
            return Triple(child, baselines.firstOrNull(), followups)\
        }\
        return null\
    }\
\
    fun deleteRecord(studyId: String) {\
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {\
            val child = repository.searchChild(studyId)\
            if (child != null) {\
                repository.deleteChildRecord(child)\
            }\
        }\
    }\
' app/src/main/java/com/example/ui/MainViewModel.kt
