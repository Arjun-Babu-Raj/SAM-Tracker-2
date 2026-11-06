package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProfileScreen(
    studyId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditBaseline: (String) -> Unit
) {
    val childData by viewModel.getChildProfileData(studyId).collectAsState(initial = null)
    
    val bgColor = Color(0xFFF8F9FF)
    val titleColor = Color(0xFF001D35)
    val subtitleColor = Color(0xFF64748B)
    
    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Child Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Record") },
                            text = { Text("Are you sure you want to delete this child's record completely? This cannot be undone.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteRecord(studyId)
                                    showDeleteDialog = false
                                    onNavigateBack()
                                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (childData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val (child, baseline, followups) = childData!!
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(child.childName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = titleColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sex: ${child.sex} | Study ID: ${child.studyId}", color = subtitleColor)
                        Text("AWC: ${child.awcName}", color = subtitleColor)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val latestStatus = followups.maxByOrNull { it.followupId }?.whz?.let { whz ->
                                when {
                                    whz < -3 -> "SAM"
                                    whz < -2 -> "MAM"
                                    else -> "Normal"
                                }
                            } ?: baseline?.programmeOutcome ?: "Unknown"
                            
                            val statusColor = when(latestStatus) {
                                "SAM" -> Color(0xFFFECACA)
                                "MAM" -> Color(0xFFFEF08A)
                                "Normal", "Recovered" -> Color(0xFFDCFCE7)
                                else -> Color(0xFFE2E8F0)
                            }
                            
                            val textColor = when(latestStatus) {
                                "SAM" -> Color(0xFF991B1B)
                                "MAM" -> Color(0xFF854D0E)
                                "Normal", "Recovered" -> Color(0xFF166534)
                                else -> Color(0xFF475569)
                            }
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Status: $latestStatus", fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = statusColor, labelColor = textColor),
                                border = null
                            )
                            
                            val hasEdema = baseline?.extraDataJson?.let {
                                try { JSONObject(it).optString("oedema") == "Present" } catch(e: Exception) { false }
                            } ?: false
                            
                            if (hasEdema) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Oedema Present", fontWeight = FontWeight.Bold) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFFEE2E2), labelColor = Color(0xFF991B1B)),
                                    border = null
                                )
                            }
                        }
                    }
                }
                
                Text("Weight Tracking (kg)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val weights = mutableListOf<Pair<String, Double>>()
                        if (baseline != null) {
                            weights.add("Baseline" to org.json.JSONObject(baseline?.extraDataJson ?: "{}").optDouble("weight_week1", 0.0))
                        }
                        followups.sortedBy { it.followupRound }.forEach { f -> 
                            val label = try {
                                val json = JSONObject(f.extraDataJson)
                                val age = json.optString("child_age_months", "")
                                if (age.isNotEmpty()) "${age}m" else f.followupRound
                            } catch (e: Exception) { f.followupRound }
                            weights.add(label to f.weight)
                        }
                        if (weights.isNotEmpty()) {
                            GrowthChartCanvas(weights)
                        } else {
                            Text("No measurements available", modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
                
                Text("WHO Z-Score Tracking (Wasting / WHZ)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val zScores = mutableListOf<Pair<String, Double>>()
                        followups.sortedBy { it.followupRound }.forEach { f -> 
                            val label = try {
                                val json = JSONObject(f.extraDataJson)
                                val age = json.optString("child_age_months", "")
                                if (age.isNotEmpty()) "${age}m" else f.followupRound
                            } catch (e: Exception) { f.followupRound }
                            zScores.add(label to f.whz)
                        }
                        if (zScores.isNotEmpty()) {
                            ZScoreChartCanvas(zScores)
                        } else {
                            Text("No follow-up Z-scores available", color = subtitleColor, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Measurement History", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                    TextButton(onClick = { onNavigateToEditBaseline(child.studyId) }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (baseline != null) "Edit Baseline" else "Add Baseline")
                    }
                }
                
                if (baseline != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Baseline Enrolment: ${org.json.JSONObject(baseline?.extraDataJson ?: "{}").optDouble("weight_week1", 0.0)}kg", fontWeight = FontWeight.Medium)
                            Text("Baseline 12-wk: ${org.json.JSONObject(baseline?.extraDataJson ?: "{}").optDouble("weight_week12", 0.0)}kg -> ${baseline.programmeOutcome}", color = subtitleColor)
                        }
                    }
                }
                
                followups.sortedBy { it.followupRound }.forEach { f ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${f.followupRound}: ${f.weight}kg (WHZ: ${f.whz})", fontWeight = FontWeight.Medium)
                            Text("Status: ${f.currentStatus}", color = subtitleColor)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun GrowthChartCanvas(dataPoints: List<Pair<String, Double>>) {
    val lineColor = Color(0xFF0061A4)
    val pointColor = Color(0xFFBA1A1A)
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width == 0f || height == 0f) return@Canvas
        
        val maxWeight = (dataPoints.maxOfOrNull { it.second } ?: 10.0) + 2.0
        val minWeight = ((dataPoints.minOfOrNull { it.second } ?: 2.0) - 2.0).coerceAtLeast(0.0)
        val weightRange = (maxWeight - minWeight).coerceAtLeast(0.1) // prevent div by zero
        
        val gap = width / (dataPoints.size.coerceAtLeast(2) - 1).toFloat()
        
        val path = Path()
        
        dataPoints.forEachIndexed { index, pair ->
            var safeX = index * gap
            var safeY = (1f - ((pair.second - minWeight) / weightRange).toFloat()) * height
            if (safeX.isNaN() || safeX.isInfinite()) safeX = 0f
            if (safeY.isNaN() || safeY.isInfinite()) safeY = 0f
            
            if (index == 0) {
                path.moveTo(safeX, safeY)
            } else {
                path.lineTo(safeX, safeY)
            }
            
            drawCircle(
                color = pointColor,
                radius = 6.dp.toPx(),
                center = Offset(safeX, safeY)
            )
            
            drawText(
                textMeasurer = textMeasurer,
                text = pair.first,
                topLeft = Offset(safeX - 20.dp.toPx(), safeY + 10.dp.toPx()),
                style = TextStyle(color = Color.DarkGray, fontSize = 10.sp)
            )
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun ZScoreChartCanvas(dataPoints: List<Pair<String, Double>>) {
    val lineColor = Color(0xFF0061A4)
    val pointColor = Color(0xFF001D35)
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width == 0f || height == 0f) return@Canvas
        
        val maxZ = 2f
        val minZ = -4f
        val zRange = maxZ - minZ
        
        val normalTop = 0f
        val normalBottom = ((maxZ - (-2f)) / zRange) * height
        drawRect(color = Color(0xFFDCFCE7), topLeft = Offset(0f, normalTop), size = androidx.compose.ui.geometry.Size(width, normalBottom - normalTop))
        
        val mamTop = normalBottom
        val mamBottom = ((maxZ - (-3f)) / zRange) * height
        drawRect(color = Color(0xFFFEF08A), topLeft = Offset(0f, mamTop), size = androidx.compose.ui.geometry.Size(width, mamBottom - mamTop))
        
        val samTop = mamBottom
        val samBottom = height
        drawRect(color = Color(0xFFFECACA), topLeft = Offset(0f, samTop), size = androidx.compose.ui.geometry.Size(width, samBottom - samTop))
        
        val strokeWidth = 1.dp.toPx()
        drawLine(Color.Gray, Offset(0f, normalBottom), Offset(width, normalBottom), strokeWidth = strokeWidth)
        drawLine(Color.Gray, Offset(0f, mamBottom), Offset(width, mamBottom), strokeWidth = strokeWidth)
        
        if (dataPoints.isEmpty()) return@Canvas
        
        val gap = if (dataPoints.size > 1) width / (dataPoints.size - 1).toFloat() else width / 2f
        val path = Path()
        
        dataPoints.forEachIndexed { index, pair ->
            var safeX = if (dataPoints.size > 1) index * gap else width / 2f
            
            // Handle NaN safely
            var zVal = pair.second.toFloat()
            if (zVal.isNaN() || zVal.isInfinite()) zVal = 0f
            zVal = zVal.coerceIn(minZ, maxZ)
            
            var safeY = ((maxZ - zVal) / zRange) * height
            if (safeX.isNaN() || safeX.isInfinite()) safeX = 0f
            if (safeY.isNaN() || safeY.isInfinite()) safeY = 0f
            
            if (index == 0) {
                path.moveTo(safeX, safeY)
            } else {
                path.lineTo(safeX, safeY)
            }
            
            drawCircle(
                color = pointColor,
                radius = 5.dp.toPx(),
                center = Offset(safeX, safeY)
            )
            
            drawText(
                textMeasurer = textMeasurer,
                text = pair.first,
                topLeft = Offset(safeX - 15.dp.toPx(), safeY + 10.dp.toPx()),
                style = TextStyle(color = Color.DarkGray, fontSize = 10.sp)
            )
        }
        
        if (dataPoints.size > 1) {
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
