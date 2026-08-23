import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

pattern = re.compile(r'\s*val dx = clientCenter\.x - userCenter\.x.*?etaMinutes = \(newDistanceKm / \(speedKmH\.coerceAtLeast\(15f\)\) \* 60f\)\.toInt\(\)\n\s*\}', re.DOTALL)
content = pattern.sub('', content)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)
