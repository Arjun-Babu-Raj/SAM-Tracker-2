with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

text = text.replace("    }\n    }\n    if (formState is FormState.Success) {", "    }\n    if (formState is FormState.Success) {")

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "w") as f:
    f.write(text)
