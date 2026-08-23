import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

replacement = """    LaunchedEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "Por favor active la ubicación para usar el mapa.", Toast.LENGTH_LONG).show()
            val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                context.startActivity(intent)
            } catch (e: Exception) {}
        }
    }"""

content = re.sub(r'    LaunchedEffect\(Unit\) \{.*?Toast\.makeText\(context, "Por favor active la ubicación para usar el mapa\.", Toast\.LENGTH_LONG\)\.show\(\)\n\s*\}\n\s*\}', replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

