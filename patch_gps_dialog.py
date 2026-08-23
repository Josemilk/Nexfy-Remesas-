import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

state_vars = "    val scope = rememberCoroutineScope()"
new_state_vars = """    val scope = rememberCoroutineScope()
    var showGpsPrompt by remember { androidx.compose.runtime.mutableStateOf(false) }"""

content = content.replace(state_vars, new_state_vars)

launched_effect_old = """    LaunchedEffect(Unit) {
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

launched_effect_new = """    LaunchedEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsPrompt = true
        }
    }"""

content = content.replace(launched_effect_old, launched_effect_new)

dialog_code = """
            if (showSearchDialog) {"""

new_dialog_code = """
            if (showGpsPrompt) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showGpsPrompt = false },
                    title = { androidx.compose.material3.Text("Activar GPS") },
                    text = { androidx.compose.material3.Text("El GPS de alta precisión está desactivado. Para que la navegación y el mapa funcionen correctamente en tiempo real, por favor activa el GPS en los ajustes de tu dispositivo.") },
                    confirmButton = {
                        androidx.compose.material3.Button(onClick = {
                            showGpsPrompt = false
                            val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir los ajustes", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            androidx.compose.material3.Text("Ir a Ajustes")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showGpsPrompt = false }) {
                            androidx.compose.material3.Text("Ignorar")
                        }
                    }
                )
            }

            if (showSearchDialog) {"""

content = content.replace(dialog_code, new_dialog_code)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

