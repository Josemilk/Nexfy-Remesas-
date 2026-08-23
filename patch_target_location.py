import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

replacement = """    LaunchedEffect(targetLocation) {
        if (targetLocation != null) {
            searchLocation = targetLocation
            recenterTrigger++
            Toast.makeText(context, "Mostrando ubicación compartida", Toast.LENGTH_SHORT).show()
        }
    }"""

content = re.sub(r'    LaunchedEffect\(targetLocation\) \{.*?\n\s*\}', replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

