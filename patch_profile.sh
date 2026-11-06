sed -i 's/import androidx.compose.material.icons.automirrored.filled.ArrowBack/import androidx.compose.material.icons.automirrored.filled.ArrowBack\nimport androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.Edit/' app/src/main/java/com/example/ui/ChildProfileScreen.kt

sed -i '/IconButton(onClick = onNavigateBack)/,/^                }/c\
                    IconButton(onClick = onNavigateBack) {\
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")\
                    }\
                },\
                actions = {\
                    var showDeleteDialog by remember { mutableStateOf(false) }\
                    IconButton(onClick = { showDeleteDialog = true }) {\
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)\
                    }\
                    if (showDeleteDialog) {\
                        AlertDialog(\
                            onDismissRequest = { showDeleteDialog = false },\
                            title = { Text("Delete Record") },\
                            text = { Text("Are you sure you want to delete this childs record? This cannot be undone.") },\
                            confirmButton = {\
                                TextButton(onClick = {\
                                    viewModel.deleteRecord(studyId)\
                                    showDeleteDialog = false\
                                    onNavigateBack()\
                                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }\
                            },\
                            dismissButton = {\
                                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }\
                            }\
                        )\
                    }\
                }' app/src/main/java/com/example/ui/ChildProfileScreen.kt
