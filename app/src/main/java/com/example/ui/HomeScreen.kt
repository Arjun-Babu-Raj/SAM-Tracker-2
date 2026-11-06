package com.example.ui
import androidx.compose.material.icons.filled.Edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToBaseline: () -> Unit,
    onNavigateToFollowup: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToDrafts: () -> Unit
) {
    val children by viewModel.allChildren.collectAsStateWithLifecycle()
    
    val activeCasesCount = children.size
    val followupsDueCount = children.size * 3 // Rough estimate for now since each has M9, M12, M15

    val bgColor = Color(0xFFF8F9FF)
    val titleColor = Color(0xFF001D35)
    val subtitleColor = Color(0xFF64748B) // slate-500
    val avatarBg = Color(0xFFD3E4FF)
    val avatarText = Color(0xFF004A77)
    val tool1Bg = Color(0xFF0061A4)
    val tool2Bg = Color(0xFFFFFFFF)
    val tool2Border = Color(0xFFE2E8F0) // slate-200

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
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
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = titleColor)
                        }
                    },
                    label = { Text("Home", fontWeight = FontWeight.Bold, color = titleColor) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToRecords,
                    icon = { Icon(Icons.Default.Timeline, contentDescription = null, tint = subtitleColor) },
                    label = { Text("Records", color = subtitleColor) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAnalytics,
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null, tint = subtitleColor) },
                    label = { Text("Analytics", color = subtitleColor) }
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
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF1F5F9))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SAM Tracker", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                    Text("Department of Community and Family Medicine,\nAll India Institute of Medical Sciences, Bhopal", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = subtitleColor)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MP", fontWeight = FontWeight.Bold, color = avatarText)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Dashboard Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Active Cases
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Text("ACTIVE CASES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$activeCasesCount", fontSize = 32.sp, fontWeight = FontWeight.Light, color = tool1Bg)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("from database", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF16A34A))
                    }

                    // Follow-ups Due
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Text("FOLLOW-UPS DUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$followupsDueCount", fontSize = 32.sp, fontWeight = FontWeight.Light, color = Color(0xFFBA1A1A))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("M9/M12/M15", fontSize = 11.sp, color = subtitleColor)
                    }
                }

                // Tool Navigation (Primary Actions)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TOOL 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp))
                            .background(tool1Bg)
                            .clickable(onClick = onNavigateToBaseline)
                            .padding(20.dp)
                            .testTag("nav_baseline"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Column {
                            Text("TOOL 1", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("BASELINE (MONTH 4-6)", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f), letterSpacing = 0.5.sp)
                            Text("Log New Recovery Admission", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.padding(top = 2.dp))
                        }
                    }

                    // TOOL 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp))
                            .background(tool2Bg)
                            .border(1.dp, tool2Border, RoundedCornerShape(32.dp))
                            .clickable(onClick = onNavigateToFollowup)
                            .padding(20.dp)
                            .testTag("nav_followup"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF0F4F8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = tool1Bg, modifier = Modifier.size(32.dp))
                        }
                        Column {
                            Text("TOOL 2", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleColor)
                            Text("FOLLOW-UP ASSESSMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tool1Bg, letterSpacing = 0.5.sp)
                            Text("Search Child ID for M9/12/15", fontSize = 14.sp, color = subtitleColor, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }

                // Offline Status Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE1F4E4))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Text("STORAGE: OFFLINE FIRST", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534), letterSpacing = 1.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("SQLite Active", fontSize = 10.sp, color = Color(0xFF15803D))
                    }
                }
            }
        }
    }
}
