with open("app/src/main/java/com/example/ui/MainViewModel.kt", "r") as f:
    text = f.read()

replacement = """    private val repository = SamRepository(AppDatabase.getDatabase(application).samDao())
    val drafts = repository.dao.getAllDrafts()
    
    init {
    }"""
import re
text = re.sub(r'    private val repository: SamRepository\s*val drafts = repository.dao.getAllDrafts\(\)\s*init \{\s*val database = AppDatabase.getDatabase\(application\)\s*repository = SamRepository\(database.samDao\(\)\)\s*\}', replacement, text)

with open("app/src/main/java/com/example/ui/MainViewModel.kt", "w") as f:
    f.write(text)
