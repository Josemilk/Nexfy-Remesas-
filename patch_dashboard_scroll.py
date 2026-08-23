import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Add imports
if "import androidx.compose.foundation.rememberScrollState" not in content:
    content = content.replace("import androidx.compose.foundation.layout.fillMaxSize", 
                              "import androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.verticalScroll")

# Make root scrollable
search_root = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {"""
replace_root = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {"""
content = content.replace(search_root, replace_root)

# Change LazyColumn to Column
search_lazy = """        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            val pendingDeliveries = deliveries.filter { it.status == com.example.data.model.DeliveryStatus.PENDING }
            items(pendingDeliveries.take(4)) { delivery ->"""
replace_lazy = """        val pendingDeliveries = deliveries.filter { it.status == com.example.data.model.DeliveryStatus.PENDING }
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (pendingDeliveries.isEmpty()) {
                Text(
                    text = "No hay entregas pendientes en este momento.",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
                )
            } else {
                pendingDeliveries.take(4).forEach { delivery ->"""
content = content.replace(search_lazy, replace_lazy)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
print("Patched DashboardScreen scroll")
