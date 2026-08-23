import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Fix clientName column
content = re.sub(r'Spacer\(modifier = Modifier\.width\(12\.dp\)\)\s*Column\s*\{', 
                 'Spacer(modifier = Modifier.width(12.dp))\n                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {', 
                 content)

# Fix Entregas pendientes column
content = re.sub(r'Spacer\(modifier = Modifier\.width\(14\.dp\)\)\s*Column\s*\{', 
                 'Spacer(modifier = Modifier.width(14.dp))\n                            Column(modifier = Modifier.weight(1f)) {', 
                 content)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
print("Patched with regex")
