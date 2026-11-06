import re

def update_file(filename, replacement_code, is_selected):
    with open(f"app/src/main/java/com/example/ui/{filename}", "r") as f:
        text = f.read()

    if "onNavigateToDrafts" not in text:
        if filename == "HomeScreen.kt":
            text = text.replace("onNavigateToAnalytics: () -> Unit", "onNavigateToAnalytics: () -> Unit,\n    onNavigateToDrafts: () -> Unit")
        elif filename == "RecordsScreen.kt":
            text = text.replace("onNavigateToProfile: (String) -> Unit", "onNavigateToProfile: (String) -> Unit,\n    onNavigateToDrafts: () -> Unit")
        elif filename == "AnalyticsScreen.kt":
            text = text.replace("onNavigateToRecords: () -> Unit", "onNavigateToRecords: () -> Unit,\n    onNavigateToDrafts: () -> Unit")

    drafts_item = f"""
                NavigationBarItem(
                    selected = {is_selected},
                    onClick = {'{ }' if is_selected == 'true' else 'onNavigateToDrafts'},
                    icon = {{
                        {'Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFFD3E4FF)).padding(horizontal = 20.dp, vertical = 4.dp)) { Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = null, tint = titleColor) }' if is_selected == 'true' else 'Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = null, tint = subtitleColor)'}
                    }},
                    label = {{ Text("Drafts", {'fontWeight = FontWeight.Bold, color = titleColor' if is_selected == 'true' else 'color = subtitleColor'}) }}{', colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)' if is_selected == 'true' else ''}
                )
            }}
        }}"""

    # We replace the closing of NavigationBar ( `            }\n        }\n    )` ) with the new item + closing
    text = re.sub(r'NavigationBarItem\([\s\S]*?\)\s*\}\s*\}\s*\)\s*\{ padding ->', lambda m: m.group(0).replace('            }\n        }\n    ) { padding ->', drafts_item + '\n    ) { padding ->'), text)
    
    with open(f"app/src/main/java/com/example/ui/{filename}", "w") as f:
        f.write(text)

update_file("HomeScreen.kt", "", "false")
update_file("RecordsScreen.kt", "", "false")
update_file("AnalyticsScreen.kt", "", "false")

