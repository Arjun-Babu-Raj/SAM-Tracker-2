package com.example.ui

import android.content.Context
import android.net.Uri
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStream

fun escapeCsvField(value: String): String {
    if (value.contains("\"") || value.contains(",") || value.contains("\n") || value.contains("\r")) {
        return "\"" + value.replace("\"", "\"\"") + "\""
    }
    return value
}

fun buildCsvExportString(
    households: Map<String, Household>,
    children: List<Child>,
    baselines: Map<String, BaselineAssessment>,
    followups: Map<String, List<LongitudinalFollowup>>
): String {
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

    if (csvBuilder.endsWith(",")) csvBuilder.setLength(csvBuilder.length - 1)
    csvBuilder.append("\n")

    for (child in children) {
        val hh = households[child.householdId]
        val b = baselines[child.studyId]
        val fs = followups[child.studyId] ?: emptyList()

        csvBuilder.append("${escapeCsvField(child.studyId)},${escapeCsvField(child.csamId)},${escapeCsvField(child.childName)},${escapeCsvField(child.sex)},${escapeCsvField(child.awcName)},${escapeCsvField(child.sectorBlockDistrict)},")
        csvBuilder.append("${escapeCsvField(hh?.householdId ?: "")},${escapeCsvField(hh?.familyType ?: "")},${hh?.totalFamilyMembers ?: ""},${hh?.monthlyIncome ?: ""},")
        csvBuilder.append("${escapeCsvField(b?.programmeOutcome ?: "")},")

        if (b != null) {
            try {
                val json = JSONObject(b.extraDataJson)
                bFields.forEach { field ->
                    val rawValue = json.optString(field, "")
                    csvBuilder.append("${escapeCsvField(rawValue)},")
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
                val weightStr = f.weight?.toString() ?: ""
                val heightStr = f.height?.toString() ?: ""
                val whzStr = f.whz?.toString() ?: ""
                csvBuilder.append("${escapeCsvField(weightStr)},${escapeCsvField(heightStr)},${escapeCsvField(whzStr)},${escapeCsvField(f.currentStatus)},")
                try {
                    val json = JSONObject(f.extraDataJson)
                    fFields.forEach { field ->
                        val rawValue = json.optString(field, "")
                        csvBuilder.append("${escapeCsvField(rawValue)},")
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
    return csvBuilder.toString()
}

fun buildBackupJsonString(
    households: List<Household>,
    children: List<Child>,
    baselines: List<BaselineAssessment>,
    followups: List<LongitudinalFollowup>
): String {
    val json = JSONObject()
    val hhArray = org.json.JSONArray()
    households.forEach { hh ->
        val obj = JSONObject()
        obj.put("householdId", hh.householdId)
        obj.put("familyType", hh.familyType)
        obj.put("totalFamilyMembers", hh.totalFamilyMembers)
        obj.put("monthlyIncome", hh.monthlyIncome)
        hhArray.put(obj)
    }
    json.put("households", hhArray)
    val chArray = org.json.JSONArray()
    children.forEach { ch ->
        val obj = JSONObject()
        obj.put("studyId", ch.studyId)
        obj.put("csamId", ch.csamId)
        obj.put("householdId", ch.householdId)
        obj.put("childName", ch.childName)
        obj.put("sex", ch.sex)
        obj.put("awcName", ch.awcName)
        obj.put("sectorBlockDistrict", ch.sectorBlockDistrict)
        chArray.put(obj)
    }
    json.put("children", chArray)
    val bArray = org.json.JSONArray()
    baselines.forEach { b ->
        val obj = JSONObject()
        obj.put("studyId", b.studyId)
        obj.put("programmeOutcome", b.programmeOutcome)
        obj.put("thrReceivedTreatment", b.thrReceivedTreatment)
        obj.put("thrConsumedDays7d", b.thrConsumedDays7d)
        obj.put("extraDataJson", b.extraDataJson)
        bArray.put(obj)
    }
    json.put("baselines", bArray)
    val fArray = org.json.JSONArray()
    followups.forEach { f ->
        val obj = JSONObject()
        obj.put("studyId", f.studyId)
        obj.put("followupRound", f.followupRound)
        obj.put("currentStatus", f.currentStatus)
        f.weight?.let { obj.put("weight", it) }
        f.height?.let { obj.put("height", it) }
        f.whz?.let { obj.put("whz", it) }
        obj.put("thrReceived", f.thrReceived)
        obj.put("thrConsumedDays7d", f.thrConsumedDays7d)
        obj.put("dietaryScore24h", f.dietaryScore24h)
        obj.put("extraDataJson", f.extraDataJson)
        fArray.put(obj)
    }
    json.put("followups", fArray)
    return json.toString(2)
}

suspend fun exportDataToCsvImpl(
    context: Context,
    uri: Uri,
    households: Map<String, Household>,
    children: List<Child>,
    baselines: Map<String, BaselineAssessment>,
    followups: Map<String, List<LongitudinalFollowup>>
) = withContext(Dispatchers.IO) {
    try {
        val csvContent = buildCsvExportString(households, children, baselines, followups)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Unable to open output stream for export")
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
