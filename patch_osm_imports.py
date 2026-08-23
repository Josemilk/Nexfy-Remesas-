with open("app/src/main/java/com/example/ui/screens/OsmMapUtils.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
"""

if "import androidx.compose.runtime.getValue" not in content:
    content = content.replace("import androidx.compose.runtime.Composable", imports + "import androidx.compose.runtime.Composable")

with open("app/src/main/java/com/example/ui/screens/OsmMapUtils.kt", "w") as f:
    f.write(content)
