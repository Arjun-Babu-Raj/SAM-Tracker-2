with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip_next = False
for i, line in enumerate(lines):
    if skip_next:
        skip_next = False
        continue
    if "onNavigateToEditBaseline" in line and "ChildProfileScreen" not in "".join(lines[max(0, i-6):i]):
        # This is the wrong one. We just skip it.
        # Wait, the line before it is onNavigateBack, which now has a comma.
        # Let's fix the comma too.
        new_lines[-1] = new_lines[-1].replace("onNavigateBack = { navController.popBackStack() },", "onNavigateBack = { navController.popBackStack() }")
        continue
    new_lines.append(line)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.writelines(new_lines)
