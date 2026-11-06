import re

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

back_handler = """    BackHandler {
        viewModel.saveDraft("baseline", dynamicAnswers, draftId)
        onNavigateBack()
    }
    
    LazyColumn("""

text = text.replace("    LazyColumn(", back_handler)

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "w") as f:
    f.write(text)
