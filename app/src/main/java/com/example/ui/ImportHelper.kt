package com.example.ui

import android.content.Context
import android.net.Uri
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

fun parseCsvRecords(reader: java.io.Reader): List<List<String>> {
    val records = mutableListOf<List<String>>()
    val currentRecord = mutableListOf<String>()
    val currentField = StringBuilder()
    var inQuotes = false
    var prevChar: Char? = null

    var intChar = reader.read()
    while (intChar != -1) {
        val c = intChar.toChar()
        when {
            c == '"' -> {
                if (inQuotes) {
                    reader.mark(1)
                    val nextInt = reader.read()
                    if (nextInt != -1 && nextInt.toChar() == '"') {
                        // Escaped quote
                        currentField.append('"')
                    } else {
                        // End of quote
                        inQuotes = false
                        reader.reset()
                    }
                } else {
                    inQuotes = true
                }
            }
            c == ',' && !inQuotes -> {
                currentRecord.add(currentField.toString())
                currentField.setLength(0)
            }
            c == '\r' && !inQuotes -> {
                // Check for \r\n
                reader.mark(1)
                val nextInt = reader.read()
                if (nextInt != -1 && nextInt.toChar() == '\n') {
                    // It's CRLF
                } else {
                    reader.reset()
                }
                currentRecord.add(currentField.toString())
                currentField.setLength(0)
                if (currentRecord.any { it.isNotBlank() }) {
                    records.add(ArrayList(currentRecord))
                }
                currentRecord.clear()
            }
            c == '\n' && !inQuotes -> {
                currentRecord.add(currentField.toString())
                currentField.setLength(0)
                if (currentRecord.any { it.isNotBlank() }) {
                    records.add(ArrayList(currentRecord))
                }
                currentRecord.clear()
            }
            else -> {
                currentField.append(c)
            }
        }
        intChar = reader.read()
    }
    if (currentField.isNotEmpty() || currentRecord.isNotEmpty()) {
        currentRecord.add(currentField.toString())
        if (currentRecord.any { it.isNotBlank() }) {
            records.add(ArrayList(currentRecord))
        }
    }
    return records
}

suspend fun importDataFromCsvReader(
    reader: java.io.Reader,
    repository: SamRepository
): Pair<Int, Int> = withContext(Dispatchers.IO) {
    val allRecords = parseCsvRecords(reader)
    if (allRecords.isEmpty()) return@withContext Pair(0, 0)
    val headers = allRecords[0].map { it.trim() }

    val bFields = QuestionBank.baselineSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }
    val fFields = QuestionBank.followupSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }
    val rounds = listOf("M9", "M12", "M15")

    var importedCount = 0
    var skippedCount = 0

    repository.runInTransaction {
        for (i in 1 until allRecords.size) {
            val cols = allRecords[i]
            if (cols.size < 6) continue

            val getCol = { headerStart: String ->
                val idx = headers.indexOfFirst { it.equals(headerStart, ignoreCase = true) }
                if (idx != -1 && idx < cols.size) cols[idx] else ""
            }

            val studyId = getCol("StudyID").trim()
            val csamId = getCol("CSAM_ID").trim()
            val childName = getCol("ChildName")
            val sex = getCol("Sex")
            val awcName = getCol("AWC")
            val sector = getCol("Sector")

            val hhId = getCol("HouseholdID").trim().takeIf { it.isNotBlank() } ?: studyId
            val fType = getCol("FamilyType")
            val fMembers = getCol("FamilyMembers").toIntOrNull() ?: 0
            val mIncome = getCol("MonthlyIncome").toDoubleOrNull() ?: 0.0

            if (studyId.isBlank() || csamId.isBlank()) {
                skippedCount++
                continue
            }

            val existingCsamChild = repository.getChildByCsamId(csamId)
            if (existingCsamChild != null && existingCsamChild.studyId != studyId) {
                throw android.database.sqlite.SQLiteConstraintException(
                    "CSAM ID '$csamId' (for Study ID '$studyId') is already assigned to Study ID '${existingCsamChild.studyId}'."
                )
            }

            val hh = Household(hhId, fType, fMembers, mIncome)
            repository.insertHousehold(hh)

            val ch = Child(studyId, csamId, hhId, childName, sex, awcName, sector)
            try {
                repository.insertChild(ch)
            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                throw android.database.sqlite.SQLiteConstraintException(
                    "CSAM ID '$csamId' (for Study ID '$studyId') clashed with an existing record."
                )
            }
            importedCount++

            // Baseline Assessment Check
            val bOutcome = getCol("Baseline_Outcome")
            val bJson = JSONObject()
            var hasBaselineData = bOutcome.isNotBlank()

            bFields.forEach { f ->
                val valFromCol = getCol("Base_${f}")
                if (valFromCol.isNotBlank()) {
                    hasBaselineData = true
                    bJson.put(f, valFromCol)
                }
            }

            if (hasBaselineData) {
                val b = BaselineAssessment(
                    studyId = studyId,
                    programmeOutcome = bOutcome.ifBlank { bJson.optString("programme_outcome", "") },
                    thrReceivedTreatment = bJson.optString("snp_provided", ""),
                    thrConsumedDays7d = bJson.optString("snp_days", "0").toIntOrNull() ?: 0,
                    extraDataJson = bJson.toString()
                )
                repository.insertBaseline(b)
            }

            // Followups
            rounds.forEach { round ->
                val fw = getCol("${round}_Weight").toDoubleOrNull()
                val fh = getCol("${round}_Height").toDoubleOrNull()
                val fwhz = getCol("${round}_WHZ").toDoubleOrNull()
                val fStatus = getCol("${round}_Status")

                val fJson = JSONObject()
                var hasFollowupData = fw != null || fh != null || fwhz != null || fStatus.isNotBlank()

                fFields.forEach { f ->
                    val valFromCol = getCol("${round}_${f}")
                    if (valFromCol.isNotBlank()) {
                        hasFollowupData = true
                        fJson.put(f, valFromCol)
                    }
                }

                if (hasFollowupData) {
                    val f = LongitudinalFollowup(
                        studyId = studyId,
                        followupRound = round,
                        currentStatus = fStatus.ifBlank { "Alive" },
                        weight = fw,
                        height = fh,
                        whz = fwhz,
                        thrReceived = fJson.optString("snp_provided", ""),
                        thrConsumedDays7d = fJson.optString("snp_days", "0").toIntOrNull() ?: 0,
                        dietaryScore24h = fJson.optString("dietary_groups", "").split(",").filter { it.isNotBlank() }.size,
                        extraDataJson = fJson.toString()
                    )
                    repository.logFollowup(f)
                }
            }
        }
    }
    Pair(importedCount, skippedCount)
}

suspend fun restoreBackupFromJsonString(
    jsonString: String,
    repository: SamRepository
): Pair<Int, Int> = withContext(Dispatchers.IO) {
    val root = JSONObject(jsonString)

    var importedChildren = 0
    var skippedChildren = 0
    val skippedStudyIds = mutableSetOf<String>()

    repository.runInTransaction {
        val hhArray = root.optJSONArray("households")
        if (hhArray != null) {
            for (i in 0 until hhArray.length()) {
                val obj = hhArray.getJSONObject(i)
                val hhId = obj.optString("householdId", "").trim()
                if (hhId.isBlank()) continue
                repository.insertHousehold(
                    Household(
                        householdId = hhId,
                        familyType = obj.optString("familyType", ""),
                        totalFamilyMembers = obj.optInt("totalFamilyMembers", 0),
                        monthlyIncome = obj.optDouble("monthlyIncome", 0.0)
                    )
                )
            }
        }

        val chArray = root.optJSONArray("children")
        if (chArray != null) {
            for (i in 0 until chArray.length()) {
                val obj = chArray.getJSONObject(i)
                val studyId = obj.optString("studyId", "").trim()
                val csamId = obj.optString("csamId", "").trim()
                val householdId = obj.optString("householdId", "").trim().ifBlank { studyId }
                if (studyId.isBlank() || csamId.isBlank()) {
                    skippedChildren++
                    if (studyId.isNotBlank()) skippedStudyIds.add(studyId)
                    continue
                }
                val existingCsamChild = repository.getChildByCsamId(csamId)
                if (existingCsamChild != null && existingCsamChild.studyId != studyId) {
                    throw android.database.sqlite.SQLiteConstraintException(
                        "CSAM ID '$csamId' (for Study ID '$studyId') is already assigned to Study ID '${existingCsamChild.studyId}'."
                    )
                }
                try {
                    repository.insertChild(
                        Child(
                            studyId = studyId,
                            csamId = csamId,
                            householdId = householdId,
                            childName = obj.optString("childName", ""),
                            sex = obj.optString("sex", ""),
                            awcName = obj.optString("awcName", ""),
                            sectorBlockDistrict = obj.optString("sectorBlockDistrict", "")
                        )
                    )
                } catch (e: android.database.sqlite.SQLiteConstraintException) {
                    throw android.database.sqlite.SQLiteConstraintException(
                        "CSAM ID '$csamId' (for Study ID '$studyId') clashed with an existing record."
                    )
                }
                importedChildren++
            }
        }

        val bArray = root.optJSONArray("baselines")
        if (bArray != null) {
            for (i in 0 until bArray.length()) {
                val obj = bArray.getJSONObject(i)
                val studyId = obj.optString("studyId", "").trim()
                if (studyId.isBlank() || studyId in skippedStudyIds) continue
                repository.insertBaseline(
                    BaselineAssessment(
                        studyId = studyId,
                        programmeOutcome = obj.optString("programmeOutcome", ""),
                        thrReceivedTreatment = obj.optString("thrReceivedTreatment", ""),
                        thrConsumedDays7d = obj.optInt("thrConsumedDays7d", 0),
                        extraDataJson = obj.optString("extraDataJson", "{}")
                    )
                )
            }
        }

        val fArray = root.optJSONArray("followups")
        if (fArray != null) {
            for (i in 0 until fArray.length()) {
                val obj = fArray.getJSONObject(i)
                val studyId = obj.optString("studyId", "").trim()
                if (studyId.isBlank() || studyId in skippedStudyIds) continue
                val fw = if (obj.has("weight") && !obj.isNull("weight")) obj.getDouble("weight") else null
                val fh = if (obj.has("height") && !obj.isNull("height")) obj.getDouble("height") else null
                val fwhz = if (obj.has("whz") && !obj.isNull("whz")) obj.getDouble("whz") else null

                repository.logFollowup(
                    LongitudinalFollowup(
                        studyId = studyId,
                        followupRound = obj.getString("followupRound"),
                        currentStatus = obj.optString("currentStatus", "Alive"),
                        weight = fw,
                        height = fh,
                        whz = fwhz,
                        thrReceived = obj.optString("thrReceived", ""),
                        thrConsumedDays7d = obj.optInt("thrConsumedDays7d", 0),
                        dietaryScore24h = obj.optInt("dietaryScore24h", 0),
                        extraDataJson = obj.optString("extraDataJson", "{}")
                    )
                )
            }
        }
    }
    Pair(importedChildren, skippedChildren)
}

suspend fun importDataFromCsvImpl(
    context: Context,
    uri: Uri,
    repository: SamRepository
): Pair<Int, Int> = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to open input stream for CSV")
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        reader.use { importDataFromCsvReader(it, repository) }
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
