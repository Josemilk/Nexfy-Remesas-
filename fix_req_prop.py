import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

content = content.replace('conn.requestProperty("User-Agent", "NexFyApp")', 'conn.setRequestProperty("User-Agent", "NexFyApp")')

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

