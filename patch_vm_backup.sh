sed -i '/fun exportDataToCsv(/i \
    fun exportBackup(context: android.content.Context, uri: android.net.Uri) {\
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {\
            try {\
                val households = repository.getAllHouseholds()\
                val children = repository.getAllChildrenList()\
                val baselines = repository.getAllBaselines()\
                val followups = repository.getAllFollowups()\
                val json = org.json.JSONObject()\
                val hhArray = org.json.JSONArray()\
                households.forEach { hh ->\
                    val obj = org.json.JSONObject()\
                    obj.put("householdId", hh.householdId)\
                    obj.put("familyType", hh.familyType)\
                    obj.put("totalFamilyMembers", hh.totalFamilyMembers)\
                    obj.put("monthlyIncome", hh.monthlyIncome)\
                    hhArray.put(obj)\
                }\
                json.put("households", hhArray)\
                val chArray = org.json.JSONArray()\
                children.forEach { ch ->\
                    val obj = org.json.JSONObject()\
                    obj.put("studyId", ch.studyId)\
                    obj.put("csamId", ch.csamId)\
                    obj.put("householdId", ch.householdId)\
                    obj.put("childName", ch.childName)\
                    obj.put("sex", ch.sex)\
                    obj.put("awcName", ch.awcName)\
                    obj.put("sectorBlockDistrict", ch.sectorBlockDistrict)\
                    chArray.put(obj)\
                }\
                json.put("children", chArray)\
                val bArray = org.json.JSONArray()\
                baselines.forEach { b ->\
                    val obj = org.json.JSONObject()\
                    obj.put("studyId", b.studyId)\
                    obj.put("weightEnrolment", b.weightEnrolment)\
                    obj.put("weight12Weeks", b.weight12Weeks)\
                    obj.put("heightEnrolment", b.heightEnrolment)\
                    obj.put("height12Weeks", b.height12Weeks)\
                    obj.put("programmeOutcome", b.programmeOutcome)\
                    obj.put("thrReceivedTreatment", b.thrReceivedTreatment)\
                    obj.put("thrConsumedDays7d", b.thrConsumedDays7d)\
                    obj.put("dietaryScore24h", b.dietaryScore24h)\
                    obj.put("extraDataJson", b.extraDataJson)\
                    bArray.put(obj)\
                }\
                json.put("baselines", bArray)\
                val fArray = org.json.JSONArray()\
                followups.forEach { f ->\
                    val obj = org.json.JSONObject()\
                    obj.put("studyId", f.studyId)\
                    obj.put("followupRound", f.followupRound)\
                    obj.put("currentStatus", f.currentStatus)\
                    obj.put("weight", f.weight)\
                    obj.put("height", f.height)\
                    obj.put("whz", f.whz)\
                    obj.put("thrReceived", f.thrReceived)\
                    obj.put("thrConsumedDays7d", f.thrConsumedDays7d)\
                    obj.put("dietaryScore24h", f.dietaryScore24h)\
                    obj.put("extraDataJson", f.extraDataJson)\
                    fArray.put(obj)\
                }\
                json.put("followups", fArray)\
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toString(2).toByteArray()) }\
            } catch (e: Exception) { e.printStackTrace() }\
        }\
    }\
\
    fun importBackup(context: android.content.Context, uri: android.net.Uri) {\
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {\
            try {\
                val jsonString = context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().use { r -> r.readText() } } ?: return@launch\
                val json = org.json.JSONObject(jsonString)\
                val hhArray = json.optJSONArray("households")\
                if (hhArray != null) {\
                    for (i in 0 until hhArray.length()) {\
                        val obj = hhArray.getJSONObject(i)\
                        repository.insertHousehold(Household(obj.getString("householdId"), obj.getString("familyType"), obj.getInt("totalFamilyMembers"), obj.getDouble("monthlyIncome")))\
                    }\
                }\
                val chArray = json.optJSONArray("children")\
                if (chArray != null) {\
                    for (i in 0 until chArray.length()) {\
                        val obj = chArray.getJSONObject(i)\
                        repository.insertChild(Child(obj.getString("studyId"), obj.getString("csamId"), obj.getString("householdId"), obj.getString("childName"), obj.getString("sex"), obj.getString("awcName"), obj.getString("sectorBlockDistrict")))\
                    }\
                }\
                val bArray = json.optJSONArray("baselines")\
                if (bArray != null) {\
                    for (i in 0 until bArray.length()) {\
                        val obj = bArray.getJSONObject(i)\
                        repository.insertBaseline(BaselineAssessment(obj.getString("studyId"), obj.getDouble("weightEnrolment"), obj.getDouble("weight12Weeks"), obj.getDouble("heightEnrolment"), obj.getDouble("height12Weeks"), obj.getString("programmeOutcome"), obj.getString("thrReceivedTreatment"), obj.getInt("thrConsumedDays7d"), obj.getInt("dietaryScore24h"), obj.getString("extraDataJson")))\
                    }\
                }\
                val fArray = json.optJSONArray("followups")\
                if (fArray != null) {\
                    for (i in 0 until fArray.length()) {\
                        val obj = fArray.getJSONObject(i)\
                        repository.logFollowup(LongitudinalFollowup(studyId = obj.getString("studyId"), followupRound = obj.getString("followupRound"), currentStatus = obj.getString("currentStatus"), weight = obj.getDouble("weight"), height = obj.getDouble("height"), whz = obj.getDouble("whz"), thrReceived = obj.getString("thrReceived"), thrConsumedDays7d = obj.getInt("thrConsumedDays7d"), dietaryScore24h = obj.getInt("dietaryScore24h"), extraDataJson = obj.getString("extraDataJson")))\
                    }\
                }\
            } catch (e: Exception) { e.printStackTrace() }\
        }\
    }\
' app/src/main/java/com/example/ui/MainViewModel.kt
