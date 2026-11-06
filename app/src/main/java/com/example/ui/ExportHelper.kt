package com.example.ui

import android.content.Context
import android.net.Uri
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStream

suspend fun exportDataToCsvImpl(
    context: Context,
    uri: Uri,
    households: Map<String, Household>,
    children: List<Child>,
    baselines: Map<String, BaselineAssessment>,
    followups: Map<String, List<LongitudinalFollowup>>
) = withContext(Dispatchers.IO) {
    try {
        val bFields = QuestionBank.baselineSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }
        val fFields = QuestionBank.followupSections.flatMap { it.fields }.filter { it !is FormField.Subheading }.map { it.id }
        
        val rounds = listOf("M9", "M12", "M15")

        val csvBuilder = StringBuilder()
        csvBuilder.append("StudyID,CSAM_ID,ChildName,Sex,AWC,Sector,")
        csvBuilder.append("HouseholdID,FamilyType,FamilyMembers,MonthlyIncome,")
        csvBuilder.append("Baseline_Outcome,")
        
        bFields.forEach { csvBuilder.append("Base_${it},") }
        
        rounds.forEach { round ->
            csvBuilder.append("${round}_Weight,${round}_Height,${round}_WHZ,${round}_Status,")
            fFields.forEach { field ->
                csvBuilder.append("${round}_${field},")
            }
        }
        
        // Remove trailing comma and add newline
        if (csvBuilder.endsWith(",")) csvBuilder.setLength(csvBuilder.length - 1)
        csvBuilder.append("\n")

        for (child in children) {
            val hh = households[child.householdId]
            val b = baselines[child.studyId]
            val fs = followups[child.studyId] ?: emptyList()

            csvBuilder.append("${child.studyId},${child.csamId},${child.childName},${child.sex},${child.awcName},${child.sectorBlockDistrict},")
            csvBuilder.append("${hh?.householdId ?: ""},${hh?.familyType ?: ""},${hh?.totalFamilyMembers ?: ""},${hh?.monthlyIncome ?: ""},")
            csvBuilder.append("${b?.programmeOutcome ?: ""},")

            if (b != null) {
                try {
                    val json = JSONObject(b.extraDataJson)
                    bFields.forEach { field ->
                        val rawValue = json.optString(field, "")
                        val value = if (rawValue.contains(",")) "\"${rawValue}\"" else rawValue.replace("\n", " ")
                        csvBuilder.append("$value,")
                    }
                } catch (e: Exception) {
                    bFields.forEach { csvBuilder.append(",") }
                }
            } else {
                bFields.forEach { csvBuilder.append(",") }
            }

            rounds.forEach { round ->
                val f = fs.find { it.followupRound.equals(round, ignoreCase = true) }
                if (f != null) {
                    csvBuilder.append("${f.weight},${f.height},${f.whz},${f.currentStatus},")
                    try {
                        val json = JSONObject(f.extraDataJson)
                        fFields.forEach { field ->
                            val rawValue = json.optString(field, "")
                            val value = if (rawValue.contains(",")) "\"${rawValue}\"" else rawValue.replace("\n", " ")
                            csvBuilder.append("$value,")
                        }
                    } catch (e: Exception) {
                        fFields.forEach { csvBuilder.append(",") }
                    }
                } else {
                    csvBuilder.append(",,,,")
                    fFields.forEach { csvBuilder.append(",") }
                }
            }

            if (csvBuilder.endsWith(",")) csvBuilder.setLength(csvBuilder.length - 1)
            csvBuilder.append("\n")
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(csvBuilder.toString().toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
