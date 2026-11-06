sed -i '/fun exportDataToCsv/,/^[[:space:]]*}$/c\
    fun exportDataToCsv(context: android.content.Context, uri: android.net.Uri) {\
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {\
            try {\
                val households = repository.getAllHouseholds().associateBy { it.householdId }\
                val children = repository.getAllChildrenList()\
                val baselines = repository.getAllBaselines().associateBy { it.studyId }\
                val followups = repository.getAllFollowups().groupBy { it.studyId }\
                exportDataToCsvImpl(context, uri, households, children, baselines, followups)\
            } catch (e: Exception) {\
                e.printStackTrace()\
            }\
        }\
    }' app/src/main/java/com/example/ui/MainViewModel.kt
