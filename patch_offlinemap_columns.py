import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

# Replace any Spacer followed by Column { with Spacer followed by Column(modifier = Modifier.weight(1f)) {
content = re.sub(r'Spacer\(modifier = Modifier\.width\(\d+\.dp\)\)\s*Column\s*\{', 
                 lambda m: m.group(0).replace("Column {", "Column(modifier = Modifier.weight(1f)) {"), 
                 content)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)
print("Patched OfflineMapScreen columns")
