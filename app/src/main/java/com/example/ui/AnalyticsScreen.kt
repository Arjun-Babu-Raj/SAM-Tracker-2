package com.example.ui
import androidx.compose.material.icons.filled.Edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToDrafts: () -> Unit
) {
    val stats by viewModel.analyticsStats.collectAsStateWithLifecycle()
    val bgColor = Color(0xFFF8F9FF)
    val titleColor = Color(0xFF001D35)
    val subtitleColor = Color(0xFF64748B)

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = subtitleColor) },
                    label = { Text("Home", color = subtitleColor) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToRecords,
                    icon = { Icon(Icons.Default.Timeline, contentDescription = null, tint = subtitleColor) },
                    label = { Text("Records", color = subtitleColor) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFD3E4FF))
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = titleColor)
                        }
                    },
                    label = { Text("Analytics", fontWeight = FontWeight.Bold, color = titleColor) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToDrafts,
                    icon = {
                        Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = null, tint = subtitleColor)
                    },
                    label = { Text("Drafts", color = subtitleColor) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Analytics Dashboard", 
                fontSize = 24.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = titleColor
            )

            // Total Children Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("TOTAL CHILDREN ENROLLED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${stats.totalEnrolled}", fontSize = 48.sp, fontWeight = FontWeight.Light, color = Color(0xFF0061A4))
                }
            }

            // Gender Distribution
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("MALES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                        Text("${stats.male}", fontSize = 24.sp, fontWeight = FontWeight.Light, color = titleColor)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("FEMALES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                        Text("${stats.female}", fontSize = 24.sp, fontWeight = FontWeight.Light, color = titleColor)
                    }
                }
            }
            
            // WHO Nutrition Status
            Text(
                "Nutrition Status (WHO Z-Scores)", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = titleColor,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("SAM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                        Text("${stats.sam}", fontSize = 22.sp, fontWeight = FontWeight.Light, color = Color(0xFFBA1A1A))
                        Text("WHZ < -3", fontSize = 10.sp, color = subtitleColor)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("MAM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        Text("${stats.mam}", fontSize = 22.sp, fontWeight = FontWeight.Light, color = Color(0xFFD97706))
                        Text("-3 ≤ WHZ < -2", fontSize = 10.sp, color = subtitleColor)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("NORMAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        Text("${stats.normal}", fontSize = 22.sp, fontWeight = FontWeight.Light, color = Color(0xFF16A34A))
                        Text("WHZ ≥ -2", fontSize = 10.sp, color = subtitleColor)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("NOT YET MEASURED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                        Text("No follow-up WHZ (e.g. Defaulter / Non-responder / Referred)", fontSize = 11.sp, color = subtitleColor)
                    }
                    Text("${stats.notYetMeasured}", fontSize = 24.sp, fontWeight = FontWeight.Light, color = titleColor)
                }
            }

            val opStatus by viewModel.operationStatus.collectAsStateWithLifecycle()
            
            when (val s = opStatus) {
                is OperationStatus.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0F2FE)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF0369A1))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Processing data...", color = Color(0xFF0369A1), fontSize = 13.sp)
                    }
                }
                is OperationStatus.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(s.message, color = Color(0xFF166534), fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.clearOperationStatus() }) {
                                Text("Dismiss", color = Color(0xFF166534), fontSize = 12.sp)
                            }
                        }
                    }
                }
                is OperationStatus.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(s.message, color = Color(0xFF991B1B), fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.clearOperationStatus() }) {
                                Text("Dismiss", color = Color(0xFF991B1B), fontSize = 12.sp)
                            }
                        }
                    }
                }
                OperationStatus.Idle -> {}
            }

            Spacer(modifier = Modifier.weight(1f))

            val context = androidx.compose.ui.platform.LocalContext.current
            val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")
            ) { uri ->
                uri?.let { viewModel.exportDataToCsv(context, it) }
            }
            
            val backupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                uri?.let { viewModel.exportBackup(context, it) }
            }
            
            val restoreJsonLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let { viewModel.restoreBackupJson(context, it) }
            }

            val importCsvLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let { viewModel.importBackupCsv(context, it) }
            }
            
            Button(
                onClick = { exportLauncher.launch("sam_tracker_export.csv") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
            ) {
                Text("Export Data to CSV", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { backupLauncher.launch("sam_backup.json") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                ) {
                    Text("Backup (JSON)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Button(
                    onClick = { restoreJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
                ) {
                    Text("Restore (JSON)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Button(
                onClick = { importCsvLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*")) },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
            ) {
                Text("Restore from CSV Export", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
