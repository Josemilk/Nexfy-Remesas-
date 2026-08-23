import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# I need to remove "Section 5: Mapas" from SettingsScreen.kt
pattern = re.compile(r'            // Section 5: Mapas\s*Card\(\s*modifier = Modifier.fillMaxWidth\(\),\s*shape = RoundedCornerShape\(20.dp\),\s*colors = CardDefaults.cardColors\(containerColor = Color.White\),\s*elevation = CardDefaults.cardElevation\(defaultElevation = 1.dp\)\s*\) \{\s*Column\(\s*modifier = Modifier.padding\(18.dp\),\s*verticalArrangement = Arrangement.spacedBy\(14.dp\)\s*\) \{.*?(?=            // Log out Button)', re.DOTALL)

if pattern.search(content):
    content = pattern.sub('', content)
    with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
        f.write(content)
    print("Removed Map settings from SettingsScreen")
else:
    print("Could not find Map settings in SettingsScreen")

