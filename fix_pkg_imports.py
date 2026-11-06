import os

for file in ["HomeScreen.kt", "RecordsScreen.kt", "AnalyticsScreen.kt", "DraftsScreen.kt"]:
    path = f"app/src/main/java/com/example/ui/{file}"
    with open(path, "r") as f:
        text = f.read()
    
    if text.startswith("import "):
        lines = text.split("\n")
        pkg_idx = -1
        for i, line in enumerate(lines):
            if line.startswith("package "):
                pkg_idx = i
                break
        
        if pkg_idx > 0:
            imports = lines[:pkg_idx]
            pkg = lines[pkg_idx]
            rest = lines[pkg_idx+1:]
            
            new_lines = [pkg] + imports + rest
            with open(path, "w") as f:
                f.write("\n".join(new_lines))

