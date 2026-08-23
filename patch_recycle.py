import re

with open("app/src/main/java/com/example/ui/screens/RecycleBinScreen.kt", "r") as f:
    content = f.read()

# Top bar column
search1 = """                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Papelera de reciclaje","""
replace1 = """                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Papelera de reciclaje","""
content = content.replace(search1, replace1)

# Item column
search2 = """                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {"""
replace2 = """                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {"""
content = content.replace(search2, replace2)

with open("app/src/main/java/com/example/ui/screens/RecycleBinScreen.kt", "w") as f:
    f.write(content)
print("Patched RecycleBinScreen")
