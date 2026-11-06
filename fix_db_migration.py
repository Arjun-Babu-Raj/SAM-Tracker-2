with open("app/src/main/java/com/example/data/Database.kt", "r") as f:
    text = f.read()

import re

# Increment version to 4
text = text.replace("version = 3,", "version = 4,")

migration_code = """val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `drafts` (`id` TEXT NOT NULL, `formType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `dataJson` TEXT NOT NULL, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_followups_studyId_followupRound` ON `followups` (`studyId`, `followupRound`)")
    }
}"""

text = text.replace("""val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `drafts` (`id` TEXT NOT NULL, `formType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `dataJson` TEXT NOT NULL, PRIMARY KEY(`id`))")
    }
}""", migration_code)

text = text.replace("addMigrations(MIGRATION_1_2, MIGRATION_2_3)", "addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)")

with open("app/src/main/java/com/example/data/Database.kt", "w") as f:
    f.write(text)
