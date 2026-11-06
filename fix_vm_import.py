with open("app/src/main/java/com/example/ui/MainViewModel.kt", "r") as f:
    text = f.read()

import_fix = """    fun importBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            importDataFromCsvImpl(context, uri, repository)
        }
    }

    fun exportDataToCsv"""

import re
text = re.sub(r'fun importBackup.*?fun exportDataToCsv', import_fix, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/MainViewModel.kt", "w") as f:
    f.write(text)

