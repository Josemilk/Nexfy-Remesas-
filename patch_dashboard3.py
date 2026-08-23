import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Line 342:
s342 = """                            Spacer(modifier = Modifier.width(14.dp))
                            Column {"""
r342 = """                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {"""
content = content.replace(s342, r342)

s513 = """                            Spacer(modifier = Modifier.width(12.dp))
                            Column {"""
r513 = """                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {"""
content = content.replace(s513, r513)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
