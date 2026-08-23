import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'Column(modifier = Modifier.weight(1f)) {',
    'Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {'
)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
print("Patched DashboardScreen.kt with padding")
