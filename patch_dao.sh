sed -i '/@Insert(onConflict = OnConflictStrategy.ABORT)/i \
    @androidx.room.Query("DELETE FROM longitudinal_followups WHERE studyId = :studyId")\
    suspend fun deleteFollowupsForChild(studyId: String)\
    @androidx.room.Query("DELETE FROM baseline_assessments WHERE studyId = :studyId")\
    suspend fun deleteBaselineForChild(studyId: String)\
    @androidx.room.Query("DELETE FROM children WHERE studyId = :studyId")\
    suspend fun deleteChild(studyId: String)\
\
    @androidx.room.Transaction\
    suspend fun deleteChildRecord(studyId: String) {\
        deleteFollowupsForChild(studyId)\
        deleteBaselineForChild(studyId)\
        deleteChild(studyId)\
    }\
' app/src/main/java/com/example/data/Database.kt
