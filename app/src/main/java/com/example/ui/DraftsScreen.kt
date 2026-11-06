package com.example.ui
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftsScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onEditDraft: (String, String) -> Unit
) {
    val drafts by viewModel.drafts.collectAsState(initial = emptyList())
    
    Scaffold(
                bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Color(0xFF64748B)) },
                    label = { Text("Home", color = Color(0xFF64748B)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToRecords,
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Timeline, contentDescription = null, tint = Color(0xFF64748B)) },
                    label = { Text("Records", color = Color(0xFF64748B)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAnalytics,
                    icon = { Icon(androidx.compose.material.icons.Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF64748B)) },
                    label = { Text("Analytics", color = Color(0xFF64748B)) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = {
                        Box(modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).background(Color(0xFFD3E4FF)).padding(horizontal = 20.dp, vertical = 4.dp)) { Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = null, tint = Color(0xFF001D35)) }
                    },
                    label = { Text("Drafts", fontWeight = FontWeight.Bold, color = Color(0xFF001D35)) }, colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Saved Drafts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (drafts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No drafts saved.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(drafts, key = { it.id }) { draft ->
                    Card(
                        onClick = { onEditDraft(draft.formType, draft.id) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (draft.formType == "baseline") "Baseline Draft" else "Follow-up Draft",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(draft.timestamp))
                                Text("Saved: $dateStr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.deleteDraft(draft.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Draft", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
