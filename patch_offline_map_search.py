import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

# Add states for search and recenter
state_vars = """    var showMapSettingsSheet by remember { mutableStateOf(false) }
    var headingUp by remember { mutableStateOf(false) }"""

new_state_vars = """    var showMapSettingsSheet by remember { mutableStateOf(false) }
    var headingUp by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var recenterTrigger by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()"""

content = content.replace(state_vars, new_state_vars)

# Replace the search box
search_box = """                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(25.dp))
                            .clickable { /* Search */ }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscar dirección o coordenadas", color = Color.Gray, fontSize = 15.sp)
                        }
                    }"""

new_search_box = """                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(25.dp))
                            .clickable { showSearchDialog = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (searchQuery.isNotEmpty()) searchQuery else "Buscar dirección o coordenadas", color = Color.Gray, fontSize = 15.sp, maxLines = 1)
                        }
                    }"""

content = content.replace(search_box, new_search_box)

# Replace center GPS button
center_gps = """                    onClick = {
                        scale = 1.0f
                        offsetX = 0f
                        offsetY = 0f
                        Toast.makeText(context, "Centrado en tu ubicación", Toast.LENGTH_SHORT).show()
                    },"""

new_center_gps = """                    onClick = {
                        searchLocation = null
                        recenterTrigger++
                        Toast.makeText(context, "Centrado en tu ubicación", Toast.LENGTH_SHORT).show()
                    },"""

content = content.replace(center_gps, new_center_gps)

# Add search dialog and update OsmMapView call
osm_map_view = """            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                mapType = settings.mapLayer,
                userLat = userLat,
                userLng = userLng,
                clientLat = targetLocation?.first ?: 23.1367,
                clientLng = targetLocation?.second ?: -82.3584,
                isNavigating = isNavigating,
                headingUp = headingUp,
                routePoints = routePoints
            )"""

new_osm_map_view = """            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                mapType = settings.mapLayer,
                userLat = userLat,
                userLng = userLng,
                clientLat = searchLocation?.first ?: (targetLocation?.first ?: 23.1367),
                clientLng = searchLocation?.second ?: (targetLocation?.second ?: -82.3584),
                isNavigating = isNavigating,
                headingUp = headingUp,
                routePoints = routePoints,
                recenterTrigger = recenterTrigger,
                searchLocation = searchLocation
            )

            if (showSearchDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showSearchDialog = false },
                    title = { Text("Buscar Ubicación") },
                    text = {
                        androidx.compose.material3.OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Dirección o coordenadas") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                showSearchDialog = false
                                if (searchQuery.isNotBlank()) {
                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        try {
                                            val geocoder = android.location.Geocoder(context, java.util.Locale("es", "CU"))
                                            val results = geocoder.getFromLocationName(searchQuery, 1)
                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                if (!results.isNullOrEmpty()) {
                                                    val loc = results[0]
                                                    searchLocation = Pair(loc.latitude, loc.longitude)
                                                    recenterTrigger++
                                                    Toast.makeText(context, "Ubicación encontrada", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Ubicación no encontrada", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                Toast.makeText(context, "Error buscando: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Buscar")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showSearchDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }"""

content = content.replace(osm_map_view, new_osm_map_view)

# import scope
if "import androidx.compose.runtime.rememberCoroutineScope" not in content:
    content = content.replace("import androidx.compose.runtime.remember", "import androidx.compose.runtime.remember\nimport androidx.compose.runtime.rememberCoroutineScope\nimport androidx.compose.runtime.mutableIntStateOf\nimport kotlinx.coroutines.launch")

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

