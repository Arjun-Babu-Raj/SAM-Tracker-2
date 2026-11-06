with open("app/src/main/java/com/example/ui/DraftsScreen.kt", "r") as f:
    text = f.read()

text = text.replace("androidx.compose.material.icons.automirrored.filled.ArrowBack", "Icons.Default.ArrowBack")

with open("app/src/main/java/com/example/ui/DraftsScreen.kt", "w") as f:
    f.write(text)
