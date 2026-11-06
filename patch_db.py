import re

with open("app/src/main/java/com/example/data/Database.kt", "r") as f:
    text = f.read()

draft_entity = """
@Entity(tableName = "drafts")
data class Draft(
    @PrimaryKey val id: String, // e.g., "baseline_uuid" or "followup_uuid"
    val formType: String, // "baseline" or "followup"
    val timestamp: Long,
    val dataJson: String
)
"""

if "data class Draft" not in text:
    text = text.replace("@Dao", draft_entity + "\n@Dao")

dao_methods = """
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: Draft)
    
    @Query("SELECT * FROM drafts ORDER BY timestamp DESC")
    fun getAllDrafts(): kotlinx.coroutines.flow.Flow<List<Draft>>
    
    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteDraft(id: String)
"""

if "insertDraft" not in text:
    text = text.replace("@Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertFollowup(followup: LongitudinalFollowup)", 
                        "@Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertFollowup(followup: LongitudinalFollowup)\n" + dao_methods)

text = text.replace("entities = [Household::class, Child::class, BaselineAssessment::class, LongitudinalFollowup::class]",
                    "entities = [Household::class, Child::class, BaselineAssessment::class, LongitudinalFollowup::class, Draft::class]")

text = text.replace("version = 2", "version = 3")

migration_3 = """
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `drafts` (`id` TEXT NOT NULL, `formType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `dataJson` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }
"""

if "MIGRATION_2_3" not in text:
    text = text.replace("val MIGRATION_1_2", migration_3 + "\n        val MIGRATION_1_2")
    text = text.replace(".addMigrations(MIGRATION_1_2)", ".addMigrations(MIGRATION_1_2, MIGRATION_2_3)")


with open("app/src/main/java/com/example/data/Database.kt", "w") as f:
    f.write(text)
