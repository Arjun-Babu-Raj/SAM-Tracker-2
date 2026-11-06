with open("app/src/main/java/com/example/ui/AnalyticsScreen.kt", "r") as f:
    text = f.read()

idx = text.find("val exportLauncher =")
if idx != -1:
    text = text[:idx]

ending = """val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")
            ) { uri ->
                uri?.let { viewModel.exportDataToCsv(context, it) }
            }
            
            val backupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                uri?.let { viewModel.exportBackup(context, it) }
            }
            
            val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let { viewModel.importBackup(context, it) }
            }
            
            Button(
                onClick = { exportLauncher.launch("sam_tracker_export.csv") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
            ) {
                Text("Export Data to CSV", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { backupLauncher.launch("sam_backup.json") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                ) {
                    Text("Backup DB", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Restore DB", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/AnalyticsScreen.kt", "w") as f:
    f.write(text + ending)
