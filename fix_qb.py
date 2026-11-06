with open("app/src/main/java/com/example/ui/QuestionBank.kt", "r") as f:
    text = f.read()

text = text.replace("    ),\n}\n\n    val followupSections", "    )\n\n    val followupSections")
text = text.replace("        ))\n    ),\n}", "        ))\n    )\n}")

with open("app/src/main/java/com/example/ui/QuestionBank.kt", "w") as f:
    f.write(text)
