with open("app/src/main/java/com/example/ui/MainViewModel.kt", "r") as f:
    text = f.read()

text = text.replace("    val drafts = repository.dao.getAllDrafts()\n", "")
text = text.replace("private val repository: SamRepository", "private val repository: SamRepository\n    val drafts = repository.dao.getAllDrafts()")

with open("app/src/main/java/com/example/ui/MainViewModel.kt", "w") as f:
    f.write(text)
