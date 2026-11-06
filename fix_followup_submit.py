import re

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "r") as f:
    text = f.read()

bad = """                                    thrReceived = "",
                                    thrConsumedDays7d = 0,
                                    dietaryScore24h = 0,"""

good = """                                    thrReceived = dynamicAnswers["snp_provided"] ?: "",
                                    thrConsumedDays7d = dynamicAnswers["snp_days"]?.toIntOrNull() ?: 0,
                                    dietaryScore24h = dynamicAnswers["dietary_groups"]?.split(",")?.filter { it.isNotBlank() }?.size ?: 0,"""

text = text.replace(bad, good)

with open("app/src/main/java/com/example/ui/FollowupScreen.kt", "w") as f:
    f.write(text)
