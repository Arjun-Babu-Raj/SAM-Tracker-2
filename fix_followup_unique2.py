with open("app/src/main/java/com/example/data/Database.kt", "r") as f:
    text = f.read()

old_entity = """@Entity(tableName = "followups",
        foreignKeys = [ForeignKey(entity = Child::class, parentColumns = ["studyId"], childColumns = ["studyId"], onDelete = ForeignKey.CASCADE)])"""

new_entity = """@Entity(tableName = "followups",
        foreignKeys = [ForeignKey(entity = Child::class, parentColumns = ["studyId"], childColumns = ["studyId"], onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["studyId", "followupRound"], unique = true)])"""

text = text.replace(old_entity, new_entity)

with open("app/src/main/java/com/example/data/Database.kt", "w") as f:
    f.write(text)
