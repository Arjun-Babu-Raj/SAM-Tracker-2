import re
with open("app/src/main/java/com/example/data/Database.kt", "r") as f:
    text = f.read()

mig34 = """
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_followups_studyId_followupRound` ON `followups` (`studyId`, `followupRound`)")
            }
        }
"""

text = text.replace("        val MIGRATION_1_2", mig34 + "        val MIGRATION_1_2")

with open("app/src/main/java/com/example/data/Database.kt", "w") as f:
    f.write(text)
