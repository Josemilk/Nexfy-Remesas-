with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

bad_string = """    LaunchedEffect(targetLocation) {
        if (targetLocation != null) {
            searchLocation = targetLocation
            recenterTrigger++
            Toast.makeText(context, "Mostrando ubicación compartida", Toast.LENGTH_SHORT).show()
        }
    }
    }"""

good_string = """    LaunchedEffect(targetLocation) {
        if (targetLocation != null) {
            searchLocation = targetLocation
            recenterTrigger++
            Toast.makeText(context, "Mostrando ubicación compartida", Toast.LENGTH_SHORT).show()
        }
    }"""

content = content.replace(bad_string, good_string)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

