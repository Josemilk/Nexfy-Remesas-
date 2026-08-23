with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "onClick = { /* Compass reset */ offsetX = 0f; offsetY = 0f },",
    "onClick = { headingUp = !headingUp; Toast.makeText(context, if (headingUp) \"Modo brújula (Heading-up)\" else \"Norte arriba (North-up)\", Toast.LENGTH_SHORT).show() },"
)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

