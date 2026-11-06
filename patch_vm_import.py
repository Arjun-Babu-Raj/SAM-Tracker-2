import re

with open("app/src/main/java/com/example/ui/MainViewModel.kt", "r") as f:
    text = f.read()

# Replace the implementation of importBackup
start_idx = text.find("fun importBackup")
if start_idx != -1:
    end_idx = text.find("}", text.find("}", start_idx) + 1)
    
    # Actually we just want to replace the whole method. It ends around line 290
    import_impl = """fun importBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            importDataFromCsvImpl(context, uri, repository)
        }
    }"""
    
    # We will regex replace the method.
    text = re.sub(r'fun importBackup.*?\}\s*\}\s*\}', import_impl, text, flags=re.DOTALL)

    with open("app/src/main/java/com/example/ui/MainViewModel.kt", "w") as f:
        f.write(text)
