with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

count = 0
for i, char in enumerate(text):
    if char == '{': count += 1
    elif char == '}': count -= 1
print(f"Final brace count: {count}")
