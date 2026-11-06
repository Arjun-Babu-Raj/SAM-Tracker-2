import re
import os

files = ["HomeScreen.kt", "RecordsScreen.kt", "AnalyticsScreen.kt"]

for file in files:
    path = f"app/src/main/java/com/example/ui/{file}"
    with open(path, "r") as f:
        text = f.read()

    # The signature of HomeScreen/RecordsScreen/AnalyticsScreen needs to have onNavigateToDrafts
    if file == "HomeScreen.kt":
        text = text.replace("onNavigateToAnalytics: () -> Unit", "onNavigateToAnalytics: () -> Unit,\n    onNavigateToDrafts: () -> Unit")
    elif file == "RecordsScreen.kt":
        text = text.replace("onNavigateToProfile: (String) -> Unit", "onNavigateToProfile: (String) -> Unit,\n    onNavigateToDrafts: () -> Unit")
    elif file == "AnalyticsScreen.kt":
        text = text.replace("onNavigateToRecords: () -> Unit", "onNavigateToRecords: () -> Unit,\n    onNavigateToDrafts: () -> Unit")

    # Add the Drafts NavigationBarItem
    nav_item = """
                NavigationBarItem(
                    selected = {is_selected},
                    onClick = {on_click},
                    icon = { {icon_content} },
                    label = { {label_content} },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
"""
    drafts_selected = "false"
    drafts_onclick = "onNavigateToDrafts"
    drafts_icon = "Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = null, tint = subtitleColor)"
    drafts_label = "Text(\"Drafts\", color = subtitleColor)"

    # Insert it at the end of the NavigationBar block, before closing brace
    # Search for NavigationBar { ... }
    
    # We will just use string replacement on the last NavigationBarItem
    if "NavigationBarItem(" in text:
        # Find the end of the last NavigationBarItem
        # Then insert the Drafts item
        # Since each file has 3 items right now, I'll just append it to the Analytics item.
        pass

    # A simpler way is to find `}        }    } { padding ->` or something similar.
    # We can match `NavigationBarItem`s and insert after the last one.
    
    # Actually, let's write a targeted replacement
    
    # Wait, we can't easily parse this with simple python script, but we can do it via regex.
    pass

