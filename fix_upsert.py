import re

with open("app/src/main/java/com/example/data/Database.kt", "r") as f:
    text = f.read()

text = text.replace("import androidx.room.Insert", "import androidx.room.Insert\nimport androidx.room.Upsert")

text = text.replace("@Insert(onConflict = OnConflictStrategy.REPLACE)", "@Upsert")

with open("app/src/main/java/com/example/data/Database.kt", "w") as f:
    f.write(text)
