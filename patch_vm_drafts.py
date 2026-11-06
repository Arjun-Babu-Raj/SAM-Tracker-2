import re

with open("app/src/main/java/com/example/ui/MainViewModel.kt", "r") as f:
    text = f.read()

draft_methods = """
    val drafts = repository.dao.getAllDrafts()

    fun saveDraft(formType: String, data: Map<String, String>, draftId: String? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val id = draftId ?: java.util.UUID.randomUUID().toString()
            val json = org.json.JSONObject(data).toString()
            repository.dao.insertDraft(com.example.data.Draft(id, formType, System.currentTimeMillis(), json))
        }
    }

    fun deleteDraft(draftId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.dao.deleteDraft(draftId)
        }
    }

    suspend fun getDraft(draftId: String): Map<String, String>? {
        return withContext(kotlinx.coroutines.Dispatchers.IO) {
            val draft = repository.dao.getAllDrafts().firstOrNull()?.find { it.id == draftId }
            draft?.let {
                val json = org.json.JSONObject(it.dataJson)
                val map = mutableMapOf<String, String>()
                json.keys().forEach { key -> map[key] = json.getString(key) }
                map
            }
        }
    }
"""
if "saveDraft" not in text:
    text = text.replace("class MainViewModel(application: Application) : AndroidViewModel(application) {", "class MainViewModel(application: Application) : AndroidViewModel(application) {\n" + draft_methods)

text = text.replace("import kotlinx.coroutines.flow.firstOrNull", "")
text = text.replace("import kotlinx.coroutines.flow.MutableStateFlow", "import kotlinx.coroutines.flow.MutableStateFlow\nimport kotlinx.coroutines.flow.firstOrNull")

with open("app/src/main/java/com/example/ui/MainViewModel.kt", "w") as f:
    f.write(text)
