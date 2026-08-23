import re

with open("app/src/main/java/com/example/ui/screens/DeliveryDetailScreen.kt", "r") as f:
    content = f.read()

# Replace all unconstrained Columns inside Rows for details
content = content.replace("Column {", "Column(modifier = Modifier.weight(1f)) {")

with open("app/src/main/java/com/example/ui/screens/DeliveryDetailScreen.kt", "w") as f:
    f.write(content)
print("Patched DeliveryDetailScreen")
