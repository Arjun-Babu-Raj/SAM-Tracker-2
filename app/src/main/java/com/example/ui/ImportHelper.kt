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

suspend fun importDataFromCsvImpl(
    context: Context,
    uri: Uri,
    repository: SamRepository
): Pair<Int, Int> = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) 
            ?: throw IllegalStateException("Unable to open input stream for CSV")
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        
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

                val hh = Household(hhId, fType, fMembers, mIncome)
                repository.insertHousehold(hh)

                val ch = Child(studyId, csamId, hhId, childName, sex, awcName, sector)
                repository.insertChild(ch)
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
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
