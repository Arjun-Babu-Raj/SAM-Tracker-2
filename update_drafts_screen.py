import re

with open("app/src/main/java/com/example/ui/DraftsScreen.kt", "r") as f:
    text = f.read()

# Change the signature to include onNavigateToHome, Records, Analytics
sig = """fun DraftsScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onEditDraft: (String, String) -> Unit
)"""
text = re.sub(r'fun DraftsScreen\([\s\S]*?onEditDraft: \(String, String\) -> Unit \/\/ formType, draftId\n\)', sig, text)

# Add NavigationBar to Scaffold
nav_bar = """        bottomBar = {
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
        topBar ="""
text = text.replace("topBar =", nav_bar)
text = text.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.foundation.background\nimport androidx.compose.ui.draw.clip")
text = text.replace("import androidx.compose.material.icons.filled.Delete", "import androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.Timeline\nimport androidx.compose.material.icons.filled.BarChart\nimport androidx.compose.material.icons.automirrored.filled.Assignment\nimport androidx.compose.material.icons.filled.Edit")


with open("app/src/main/java/com/example/ui/DraftsScreen.kt", "w") as f:
    f.write(text)
