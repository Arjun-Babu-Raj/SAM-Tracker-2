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
    val weight: Double,
    val height: Double,
    val whz: Double,
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

    @Upsert
    suspend fun insertBaseline(baseline: BaselineAssessment)

    @Transaction
    suspend fun logBaselineTransaction(household: Household, child: Child, baseline: BaselineAssessment) {
        insertHousehold(household)
        insertChild(child)
        insertBaseline(baseline)
    }

    @Query("SELECT * FROM children WHERE studyId = :query OR csamId = :query OR childName LIKE '%' || :query || '%' LIMIT 1")
    suspend fun getChildByStudyOrCsamId(query: String): Child?

    @Query("SELECT * FROM children ORDER BY childName ASC")
    fun getAllChildren(): kotlinx.coroutines.flow.Flow<List<Child>>

    @Query("SELECT * FROM households")
    suspend fun getAllHouseholds(): List<Household>

    @Query("SELECT * FROM children")
    suspend fun getAllChildrenList(): List<Child>

    @Query("SELECT * FROM baseline_assessments")
    suspend fun getAllBaselines(): List<BaselineAssessment>

    @Query("SELECT * FROM longitudinal_followups")
    suspend fun getAllFollowups(): List<LongitudinalFollowup>

    @androidx.room.Query("DELETE FROM longitudinal_followups WHERE studyId = :studyId")
    suspend fun deleteFollowupsForChild(studyId: String)
    @androidx.room.Query("DELETE FROM baseline_assessments WHERE studyId = :studyId")
    suspend fun deleteBaselineForChild(studyId: String)
    @androidx.room.Query("DELETE FROM children WHERE studyId = :studyId")
    suspend fun deleteChild(studyId: String)

    @androidx.room.Transaction
    suspend fun deleteChildRecord(studyId: String) {
        deleteFollowupsForChild(studyId)
        deleteBaselineForChild(studyId)
        deleteChild(studyId)
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
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun samDao(): SamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
        
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
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM `longitudinal_followups` WHERE `followupId` NOT IN (SELECT MIN(`followupId`) FROM `longitudinal_followups` GROUP BY `studyId`, `followupRound`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_longitudinal_followups_studyId_followupRound` ON `longitudinal_followups` (`studyId`, `followupRound`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_children_csamId` ON `children` (`csamId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_children_householdId` ON `children` (`householdId`)")
            }
        }
            return INSTANCE ?: synchronized(this) {
                // Initialize database and enforce PRAGMA foreign_keys = ON via fallbackToDestructiveMigration if needed 
                // Note: Room enables foreign keys by default on standard configurations, 
                // but setting a specific callback can verify or explicitly execute.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sam_tracking_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
