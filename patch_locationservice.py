import re

with open("app/src/main/java/com/example/service/LocationService.kt", "r") as f:
    content = f.read()

# Update intervals
content = content.replace("2000L,", "1000L,")
content = content.replace("3000L,", "1000L,")
content = content.replace("1f,", "0f,")
content = content.replace("2f,", "0f,")

# Remove simulation
pattern = re.compile(r'\s*// Start fallback position simulation job so GPS coordinates and speed are active.*?delay\(3000\)\n\s*\}\n\s*\}', re.DOTALL)
content = pattern.sub('', content)

with open("app/src/main/java/com/example/service/LocationService.kt", "w") as f:
    f.write(content)

