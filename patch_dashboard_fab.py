import re
with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

if "FloatingActionButton" not in content:
    content = content.replace("import androidx.compose.foundation.layout.fillMaxSize", "import androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.material3.FloatingActionButton")
    content = content.replace("import androidx.compose.material.icons.filled.Send", "import androidx.compose.material.icons.filled.Send\nimport androidx.compose.material.icons.filled.Map")

search = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {"""
replace = """    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {"""

if search in content:
    content = content.replace(search, replace)
    content = content.replace("    }\n}\n", """    }\n
        FloatingActionButton(
            onClick = { /* navigate to offline map */ },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            containerColor = Color(0xFF2563EB),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Map, contentDescription = "Mapa offline")
        }
    }\n}\n""")

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
print("Patched Dashboard")
