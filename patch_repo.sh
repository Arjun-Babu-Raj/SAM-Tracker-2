sed -i '/suspend fun deleteChildRecord/a \
    suspend fun insertHousehold(hh: Household) = withContext(Dispatchers.IO) { dao.insertHousehold(hh) }\
    suspend fun insertChild(ch: Child) = withContext(Dispatchers.IO) { dao.insertChild(ch) }\
    suspend fun insertBaseline(b: BaselineAssessment) = withContext(Dispatchers.IO) { dao.insertBaseline(b) }\
' app/src/main/java/com/example/data/Repository.kt
