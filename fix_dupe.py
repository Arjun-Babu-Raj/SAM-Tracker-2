with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    lines = f.readlines()

out = []
seen = False
for line in lines:
    if "onNavigateToDrafts = {" in line and "HomeScreen" in "".join(out[-15:]):
        if not seen:
            seen = True
            out.append(line)
        else:
            # skip this line and next 4 lines
            pass
    else:
        out.append(line)

# Let's do it safer with regex
import re
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

# Just remove the second one in HomeScreen
text = text.replace("""                onNavigateToAnalytics = { navController.navigate("analytics") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } },
                onNavigateToDrafts = { navController.navigate("drafts") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } }""", """                onNavigateToAnalytics = { navController.navigate("analytics") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } }""")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
