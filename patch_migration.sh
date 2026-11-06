sed -i '/fun getDatabase(context: Context): AppDatabase {/a \
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {\
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {\
                db.execSQL("DELETE FROM `longitudinal_followups` WHERE `followupId` NOT IN (SELECT MIN(`followupId`) FROM `longitudinal_followups` GROUP BY `studyId`, `followupRound`)")\
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_longitudinal_followups_studyId_followupRound` ON `longitudinal_followups` (`studyId`, `followupRound`)")\
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_children_csamId` ON `children` (`csamId`)")\
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_children_householdId` ON `children` (`householdId`)")\
            }\
        }' app/src/main/java/com/example/data/Database.kt

sed -i 's/\.fallbackToDestructiveMigration()/.addMigrations(MIGRATION_1_2)\n                .fallbackToDestructiveMigration()/' app/src/main/java/com/example/data/Database.kt
