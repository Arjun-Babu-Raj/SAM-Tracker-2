package com.example.ui
import androidx.compose.material.icons.filled.Edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RecordsScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToDrafts: () -> Unit
) {
    val children by viewModel.allChildren.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredChildren = children.filter {
        it.childName.contains(searchQuery, ignoreCase = true) ||
        it.studyId.contains(searchQuery, ignoreCase = true) ||
        it.csamId.contains(searchQuery, ignoreCase = true)
    }

    // Define colors perfectly mirroring sleek interface bottom nav
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
                    selected = true,
                    onClick = { },
                    icon = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFD3E4FF))
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = titleColor)
                        }
                    },
                    label = { Text("Records", fontWeight = FontWeight.Bold, color = titleColor) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
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
                .padding(20.dp)
        ) {
            Text(
                "Enrolled Children", 
                fontSize = 24.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = titleColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Name or ID...", color = subtitleColor) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = subtitleColor) },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFD3E4FF)
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (filteredChildren.isEmpty()) {
                Text(
                    "No records found. Enrol children via Tool 1 on the dashboard.", 
                    color = subtitleColor
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredChildren, key = { it.studyId }) { child ->
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToProfile(child.studyId) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                Text(
                                    text = child.childName, 
                                    fontWeight = FontWeight.Bold, 
                                    color = titleColor, 
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Study ID: ${child.studyId} | CSAM ID: ${child.csamId}", color = subtitleColor, fontSize = 14.sp)
                                Text("AWC: ${child.awcName}", color = subtitleColor, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
