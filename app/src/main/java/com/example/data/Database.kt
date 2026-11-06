package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Upsert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction

@Entity(tableName = "households")
data class Household(
    @PrimaryKey val householdId: String,
    val familyType: String,
    val totalFamilyMembers: Int,
    val monthlyIncome: Double
)

@Entity(
    tableName = "children",
    foreignKeys = [
        ForeignKey(
            entity = Household::class,
            parentColumns = ["householdId"],
            childColumns = ["householdId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["csamId"], unique = true),
        Index(value = ["householdId"])
    ]
)
data class Child(
    @PrimaryKey val studyId: String,
    val csamId: String,
    val householdId: String,
    val childName: String,
    val sex: String,
    val awcName: String,
    val sectorBlockDistrict: String
)

@Entity(
    tableName = "baseline_assessments",
    foreignKeys = [
        ForeignKey(
            entity = Child::class,
            parentColumns = ["studyId"],
            childColumns = ["studyId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class BaselineAssessment(
    @PrimaryKey val studyId: String,
    val programmeOutcome: String,
    val thrReceivedTreatment: String,
    val thrConsumedDays7d: Int,
    val extraDataJson: String
)

@Entity(
    tableName = "longitudinal_followups",
    foreignKeys = [
        ForeignKey(
            entity = Child::class,
            parentColumns = ["studyId"],
            childColumns = ["studyId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["studyId", "followupRound"], unique = true)]
)
data class LongitudinalFollowup(
    @PrimaryKey(autoGenerate = true) val followupId: Int = 0,
    val studyId: String,
    val followupRound: String, // 'M9', 'M12', or 'M15'
    val currentStatus: String, // 'Alive' or 'Dead'
    val weight: Double? = null,
    val height: Double? = null,
    val whz: Double? = null,
    val thrReceived: String,
    val thrConsumedDays7d: Int,
    val dietaryScore24h: Int,
    val extraDataJson: String
)


@Entity(tableName = "drafts")
data class Draft(
    @PrimaryKey val id: String, // e.g., "baseline_uuid" or "followup_uuid"
    val formType: String, // "baseline" or "followup"
    val timestamp: Long,
    val dataJson: String
)

@Dao
interface SamDao {
    @Upsert
    suspend fun insertHousehold(household: Household)

    @Upsert
    suspend fun insertChild(child: Child)

    @Query("SELECT * FROM children WHERE studyId = :studyId LIMIT 1")
    suspend fun getChildByStudyId(studyId: String): Child?

    @Query("SELECT * FROM children WHERE csamId = :csamId LIMIT 1")
    suspend fun getChildByCsamId(csamId: String): Child?

    @Upsert
    suspend fun insertBaseline(baseline: BaselineAssessment)

    @Transaction
    suspend fun logBaselineTransaction(household: Household, child: Child, baseline: BaselineAssessment) {
        insertHousehold(household)
        insertChild(child)
        insertBaseline(baseline)
    }

    // Exact match on studyId or csamId first, then exact name, then prefix/contains
    @Query("SELECT * FROM children WHERE studyId = :query OR csamId = :query LIMIT 1")
    suspend fun getChildByExactStudyOrCsamId(query: String): Child?

    @Query("SELECT * FROM children WHERE childName = :query COLLATE NOCASE LIMIT 1")
    suspend fun getChildByExactName(query: String): Child?

    @Query("SELECT * FROM children WHERE studyId = :query OR csamId = :query OR childName LIKE '%' || :query || '%' ORDER BY CASE WHEN studyId = :query THEN 1 WHEN csamId = :query THEN 2 WHEN childName = :query COLLATE NOCASE THEN 3 ELSE 4 END LIMIT 1")
    suspend fun getChildByStudyOrCsamId(query: String): Child?

    @Query("SELECT * FROM children ORDER BY childName ASC")
    fun getAllChildren(): kotlinx.coroutines.flow.Flow<List<Child>>

    @Query("SELECT * FROM households")
    suspend fun getAllHouseholds(): List<Household>

    @Query("SELECT * FROM children")
    suspend fun getAllChildrenList(): List<Child>

    @Query("SELECT * FROM baseline_assessments")
    suspend fun getAllBaselines(): List<BaselineAssessment>

    @Query("SELECT * FROM baseline_assessments")
    fun getAllBaselinesFlow(): kotlinx.coroutines.flow.Flow<List<BaselineAssessment>>

    @Query("SELECT * FROM longitudinal_followups")
    suspend fun getAllFollowups(): List<LongitudinalFollowup>

    @Query("SELECT * FROM longitudinal_followups")
    fun getAllFollowupsFlow(): kotlinx.coroutines.flow.Flow<List<LongitudinalFollowup>>

    @Query("SELECT * FROM children WHERE studyId = :id LIMIT 1")
    fun childFlow(id: String): kotlinx.coroutines.flow.Flow<Child?>

    @Query("SELECT * FROM baseline_assessments WHERE studyId = :id LIMIT 1")
    fun baselineFlow(id: String): kotlinx.coroutines.flow.Flow<BaselineAssessment?>

    @Query("SELECT * FROM longitudinal_followups WHERE studyId = :id")
    fun followupsFlow(id: String): kotlinx.coroutines.flow.Flow<List<LongitudinalFollowup>>

    @Query("SELECT * FROM longitudinal_followups WHERE studyId = :studyId AND followupRound = :round LIMIT 1")
    suspend fun getFollowupByStudyIdAndRound(studyId: String, round: String): LongitudinalFollowup?

    @androidx.room.Query("DELETE FROM longitudinal_followups WHERE studyId = :studyId")
    suspend fun deleteFollowupsForChild(studyId: String)
    @androidx.room.Query("DELETE FROM baseline_assessments WHERE studyId = :studyId")
    suspend fun deleteBaselineForChild(studyId: String)
    @androidx.room.Query("DELETE FROM children WHERE studyId = :studyId")
    suspend fun deleteChild(studyId: String)
    @androidx.room.Query("DELETE FROM households WHERE householdId NOT IN (SELECT DISTINCT householdId FROM children)")
    suspend fun deleteOrphanedHouseholds()

    @androidx.room.Transaction
    suspend fun deleteChildRecord(studyId: String) {
        deleteFollowupsForChild(studyId)
        deleteBaselineForChild(studyId)
        deleteChild(studyId)
        deleteOrphanedHouseholds()
    }

    @Upsert
    suspend fun insertFollowup(followup: LongitudinalFollowup)

    @Upsert
    suspend fun insertDraft(draft: Draft)
    
    @Query("SELECT * FROM drafts ORDER BY timestamp DESC")
    fun getAllDrafts(): kotlinx.coroutines.flow.Flow<List<Draft>>
    
    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteDraft(id: String)

}

@Database(
    entities = [Household::class, Child::class, BaselineAssessment::class, LongitudinalFollowup::class, Draft::class],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun samDao(): SamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM `longitudinal_followups` WHERE `followupId` NOT IN (SELECT MIN(`followupId`) FROM `longitudinal_followups` GROUP BY `studyId`, `followupRound`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_longitudinal_followups_studyId_followupRound` ON `longitudinal_followups` (`studyId`, `followupRound`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_children_csamId` ON `children` (`csamId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_children_householdId` ON `children` (`householdId`)")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `drafts` (`id` TEXT NOT NULL, `formType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `dataJson` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_longitudinal_followups_studyId_followupRound` ON `longitudinal_followups` (`studyId`, `followupRound`)")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `longitudinal_followups_new` (
                        `followupId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `studyId` TEXT NOT NULL,
                        `followupRound` TEXT NOT NULL,
                        `currentStatus` TEXT NOT NULL,
                        `weight` REAL,
                        `height` REAL,
                        `whz` REAL,
                        `thrReceived` TEXT NOT NULL,
                        `thrConsumedDays7d` INTEGER NOT NULL,
                        `dietaryScore24h` INTEGER NOT NULL,
                        `extraDataJson` TEXT NOT NULL,
                        FOREIGN KEY(`studyId`) REFERENCES `children`(`studyId`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )"""
                )
                db.execSQL(
                    """INSERT INTO `longitudinal_followups_new`
                        SELECT `followupId`, `studyId`, `followupRound`, `currentStatus`,
                        NULLIF(`weight`, 0), NULLIF(`height`, 0), CASE WHEN `whz` = 0 AND `weight` = 0 THEN NULL ELSE `whz` END,
                        `thrReceived`, `thrConsumedDays7d`, `dietaryScore24h`, `extraDataJson`
                        FROM `longitudinal_followups`"""
                )
                db.execSQL("DROP TABLE `longitudinal_followups`")
                db.execSQL("ALTER TABLE `longitudinal_followups_new` RENAME TO `longitudinal_followups`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_longitudinal_followups_studyId_followupRound` ON `longitudinal_followups` (`studyId`, `followupRound`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sam_tracking_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
