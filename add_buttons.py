import re

with open("app/src/main/java/com/example/ui/AnalyticsScreen.kt", "r") as f:
    text = f.read()

replacement = '''Button(
                onClick = { exportLauncher.launch("sam_tracker_export.csv") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)) // Teal
            ) {
                Text("Export Data to CSV", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { backupLauncher.launch("sam_backup.json") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                ) {
                    Text("Backup DB", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Restore DB", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }'''

text = re.sub(r'Button\(\s*onClick = \{ exportLauncher\.launch.*?\}\s*\)', replacement, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/AnalyticsScreen.kt", "w") as f:
    f.write(text)
