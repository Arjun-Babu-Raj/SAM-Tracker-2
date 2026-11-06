package com.example.ui

import android.content.Context
import android.net.Uri
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

suspend fun importDataFromCsvImpl(
    context: Context,
    uri: Uri,
    repository: SamRepository
) = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext
        val reader = BufferedReader(InputStreamReader(inputStream))
        val headerLine = reader.readLine() ?: return@withContext
        val headers = headerLine.split(",")
        
        val bFields = QuestionBank.baselineSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }
        val fFields = QuestionBank.followupSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }
        val rounds = listOf("M9", "M12", "M15")

        var line = reader.readLine()
        while (line != null) {
                        val cols = mutableListOf<String>()
            var current = java.lang.StringBuilder()
            var inQuotes = false
            for (char in line) {
                if (char == '"') {
                    inQuotes = !inQuotes
                } else if (char == ',' && !inQuotes) {
                    cols.add(current.toString().trim())
                    current = java.lang.StringBuilder()
                } else {
                    current.append(char)
                }
            }
            cols.add(current.toString().trim())
            if (cols.size < 10) {
                line = reader.readLine()
                continue
            }
            val getCol = { headerStart: String -> 
                val idx = headers.indexOfFirst { it.equals(headerStart, ignoreCase = true) }
                if (idx != -1 && idx < cols.size) cols[idx] else ""
            }

            val studyId = getCol("StudyID")
            val csamId = getCol("CSAM_ID")
            val childName = getCol("ChildName")
            val sex = getCol("Sex")
            val awcName = getCol("AWC")
            val sector = getCol("Sector")

            val hhId = getCol("HouseholdID")
            val fType = getCol("FamilyType")
            val fMembers = getCol("FamilyMembers").toIntOrNull() ?: 0
            val mIncome = getCol("MonthlyIncome").toDoubleOrNull() ?: 0.0

            if (studyId.isNotBlank() && hhId.isNotBlank()) {
                val hh = Household(hhId, fType, fMembers, mIncome)
                repository.insertHousehold(hh)

                val ch = Child(studyId, csamId, hhId, childName, sex, awcName, sector)
                repository.insertChild(ch)

                // Baseline
                val bEnrol = getCol("Baseline_WeightEnrol").toDoubleOrNull()
                if (bEnrol != null) {
                    val bWeight12 = getCol("Baseline_Weight12").toDoubleOrNull() ?: 0.0
                    val bHeightEnrol = getCol("Baseline_HeightEnrol").toDoubleOrNull() ?: 0.0
                    val bHeight12 = getCol("Baseline_Height12").toDoubleOrNull() ?: 0.0
                    val bOutcome = getCol("Baseline_Outcome")
                    
                    val bJson = JSONObject()
                    bFields.forEach { f -> bJson.put(f, getCol("Base_${f}")) }
                    
                    val b = BaselineAssessment(
                        studyId = studyId,
                        programmeOutcome = bOutcome,
                        thrReceivedTreatment = bJson.optString("snp_provided", ""),
                        thrConsumedDays7d = bJson.optString("snp_days", "0").toIntOrNull() ?: 0,
                        extraDataJson = bJson.toString()
                    )
                    repository.insertBaseline(b)
                }

                // Followups
                rounds.forEach { round ->
                    val fw = getCol("${round}_Weight").toDoubleOrNull()
                    if (fw != null) {
                        val fh = getCol("${round}_Height").toDoubleOrNull() ?: 0.0
                        val fwhz = getCol("${round}_WHZ").toDoubleOrNull() ?: 0.0
                        val fStatus = getCol("${round}_Status")

                        val fJson = JSONObject()
                        fFields.forEach { f -> fJson.put(f, getCol("${round}_${f}")) }

                        val f = LongitudinalFollowup(
                            studyId = studyId,
                            followupRound = round,
                            currentStatus = fStatus,
                            weight = fw,
                            height = fh,
                            whz = fwhz,
                            thrReceived = "",
                            thrConsumedDays7d = 0,
                            dietaryScore24h = 0,
                            extraDataJson = fJson.toString()
                        )
                        repository.logFollowup(f)
                    }
                }
            }

            line = reader.readLine()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
