with open("app/src/main/java/com/example/ui/MainViewModel.kt", "r") as f:
    text = f.read()

text = text.replace("import kotlinx.coroutines.withContext\npackage com.example.ui", "package com.example.ui\nimport kotlinx.coroutines.withContext")

with open("app/src/main/java/com/example/ui/MainViewModel.kt", "w") as f:
    f.write(text)
