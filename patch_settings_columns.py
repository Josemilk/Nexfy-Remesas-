import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

content = re.sub(r'Spacer\(modifier = Modifier\.width\(\d+\.dp\)\)\s*Column\s*\{', 
                 lambda m: m.group(0).replace("Column {", "Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {"), 
                 content)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
print("Patched SettingsScreen columns")
