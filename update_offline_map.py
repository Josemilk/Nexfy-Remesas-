import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

# 1. Add new state variables
state_vars_pattern = r'var searchLocation by remember \{ mutableStateOf<Pair<Double, Double>\?>\(null\) \}'
new_state_vars = """var searchLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var customMarkers by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }
    var showNavigationSetup by remember { mutableStateOf(false) }
    var navStartType by remember { mutableStateOf("Mi Ubicación") }
    var navEndType by remember { mutableStateOf("Búsqueda/Marcador") }
    var isMapCentered by remember { mutableStateOf(true) }
    var navStartLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var navEndLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }"""
content = re.sub(state_vars_pattern, new_state_vars, content)


# 2. Update OsmMapView call
osm_map_call_pattern = r'OsmMapView\(\s*modifier = Modifier\.fillMaxSize\(\),\s*mapType = settings\.mapLayer,\s*userLat = userLat,\s*userLng = userLng,\s*clientLat = searchLocation\?\.first \?: \(targetLocation\?\.first \?: 23\.1367\),\s*clientLng = searchLocation\?\.second \?: \(targetLocation\?\.second \?: -82\.3584\),\s*isNavigating = isNavigating,\s*headingUp = headingUp,\s*routePoints = routePoints,\s*recenterTrigger = recenterTrigger,\s*searchLocation = searchLocation\s*\)'

new_osm_map_call = """OsmMapView(
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
                searchLocation = searchLocation,
                customMarkers = customMarkers,
                onMapLongClick = { lat, lng ->
                    customMarkers = customMarkers + Pair(lat, lng)
                    Toast.makeText(context, "Marcador guardado en $lat, $lng", Toast.LENGTH_SHORT).show()
                },
                onMapScrolled = {
                    isMapCentered = false
                },
                onMapClick = { lat, lng ->
                    searchLocation = Pair(lat, lng)
                    Toast.makeText(context, "Destino seleccionado", Toast.LENGTH_SHORT).show()
                }
            )"""
content = re.sub(osm_map_call_pattern, new_osm_map_call, content, flags=re.DOTALL)


# 3. Update location button color
loc_btn_pattern = r'FloatingActionButton\(\s*onClick = \{\s*searchLocation = null\s*recenterTrigger\+\+\s*Toast\.makeText\(context, "Centrado en tu ubicación", Toast\.LENGTH_SHORT\)\.show\(\)\s*\},.*?Icon\(Icons\.Default\.MyLocation, contentDescription = "Centrar GPS"\)\s*\}'

new_loc_btn = """FloatingActionButton(
                    onClick = {
                        searchLocation = null
                        isMapCentered = true
                        recenterTrigger++
                        Toast.makeText(context, "Centrado en tu ubicación", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = if (isMapCentered) Color(0xFF2563EB) else Color.White.copy(alpha = 0.7f),
                    contentColor = if (isMapCentered) Color.White else Color(0xFF2563EB),
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = if (isMapCentered) 6.dp else 0.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar GPS")
                }"""
content = re.sub(loc_btn_pattern, new_loc_btn, content, flags=re.DOTALL)


# 4. Update navigation button
nav_btn_pattern = r'FloatingActionButton\(\s*onClick = \{ isNavigating = !isNavigating \},\s*containerColor = if \(isNavigating\) Color\(0xFFEF4444\) else Color\(0xFF2563EB\),\s*contentColor = Color\.White,\s*modifier = Modifier\.size\(56\.dp\),\s*shape = CircleShape\s*\)\s*\{\s*Icon\(\s*if \(isNavigating\) Icons\.Default\.Close else Icons\.Default\.Directions,\s*contentDescription = "Navegar"\s*\)\s*\}'

new_nav_btn = """FloatingActionButton(
                    onClick = { 
                        if (isNavigating) {
                            isNavigating = false
                        } else {
                            showNavigationSetup = true
                        }
                    },
                    containerColor = if (isNavigating) Color(0xFFEF4444) else Color(0xFF2563EB),
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        if (isNavigating) Icons.Default.Close else Icons.Default.Directions, 
                        contentDescription = "Navegar"
                    )
                }"""
content = re.sub(nav_btn_pattern, new_nav_btn, content, flags=re.DOTALL)


# 5. Add Navigation Setup Dialog inside the screen
dialog_insertion = r'if \(showMapSettingsSheet\) \{'
new_dialog = """
            if (showNavigationSetup) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showNavigationSetup = false },
                    title = { Text("Configurar Navegación") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Origen:", fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(
                                    onClick = { navStartType = "Mi Ubicación" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (navStartType == "Mi Ubicación") Color(0xFF2563EB) else Color.LightGray)
                                ) { Text("Mi Ubicación", fontSize = 12.sp) }
                                Button(
                                    onClick = { navStartType = "Marcador" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (navStartType == "Marcador") Color(0xFF2563EB) else Color.LightGray)
                                ) { Text("Marcador Seleccionado", fontSize = 12.sp) }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Destino:", fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(
                                    onClick = { navEndType = "Búsqueda" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (navEndType == "Búsqueda") Color(0xFF2563EB) else Color.LightGray)
                                ) { Text("Búsqueda", fontSize = 12.sp) }
                                Button(
                                    onClick = { navEndType = "Marcador" },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (navEndType == "Marcador") Color(0xFF2563EB) else Color.LightGray)
                                ) { Text("Marcador Seleccionado", fontSize = 12.sp) }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            showNavigationSetup = false
                            
                            // Determine Start
                            if (navStartType == "Mi Ubicación") {
                                navStartLocation = Pair(userLat, userLng)
                            } else {
                                navStartLocation = searchLocation ?: customMarkers.lastOrNull()
                            }
                            
                            // Determine End
                            if (navEndType == "Búsqueda") {
                                navEndLocation = searchLocation ?: targetLocation
                            } else {
                                navEndLocation = searchLocation ?: customMarkers.lastOrNull()
                            }
                            
                            if (navStartLocation == null || navEndLocation == null) {
                                Toast.makeText(context, "Falta seleccionar origen o destino", Toast.LENGTH_SHORT).show()
                            } else {
                                isNavigating = true
                            }
                        }) {
                            Text("Iniciar")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showNavigationSetup = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showMapSettingsSheet) {"""
content = content.replace(dialog_insertion, new_dialog)


# 6. Update LaunchedEffect for routing
route_pattern = r'LaunchedEffect\(isNavigating, transportMode, searchLocation, targetLocation\).*?else \{\s*routePoints = emptyList\(\)\s*\}\s*\}'

new_route = """LaunchedEffect(isNavigating, transportMode, navStartLocation, navEndLocation) {
        if (isNavigating && navStartLocation != null && navEndLocation != null) {
            tts.value?.speak("Iniciando navegación", TextToSpeech.QUEUE_FLUSH, null, null)
            val destLat = navEndLocation!!.first
            val destLng = navEndLocation!!.second
            
            while (isNavigating) {
                try {
                    val mode = when (transportMode) {
                        "foot" -> "foot"
                        "bike" -> "bike"
                        else -> "driving"
                    }
                    val currentLat = if (navStartType == "Mi Ubicación") viewModel.userLatitude.value else navStartLocation!!.first
                    val currentLng = if (navStartType == "Mi Ubicación") viewModel.userLongitude.value else navStartLocation!!.second
                    
                    val urlStr = "https://router.project-osrm.org/route/v1/$mode/$currentLng,$currentLat;$destLng,$destLat?overview=full&geometries=geojson"
                    val url = java.net.URL(urlStr)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("User-Agent", "NexFyApp")
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    
                    if (conn.responseCode == 200) {
                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = org.json.JSONObject(response)
                        val routes = json.optJSONArray("routes")
                        if (routes != null && routes.length() > 0) {
                            val route = routes.getJSONObject(0)
                            val distance = route.optDouble("distance") // in meters
                            val duration = route.optDouble("duration") // in seconds
                            
                            distanceKm = (distance / 1000.0).toFloat()
                            etaMinutes = (duration / 60.0).toInt()
                            
                            val geometry = route.getJSONObject("geometry")
                            val coordinates = geometry.getJSONArray("coordinates")
                            val points = mutableListOf<Pair<Double, Double>>()
                            for (i in 0 until coordinates.length()) {
                                val point = coordinates.getJSONArray(i)
                                points.add(Pair(point.getDouble(1), point.getDouble(0)))
                            }
                            routePoints = points
                            
                            if (distance < 50) {
                                navStep = "¡Ha llegado al destino!"
                                tts.value?.speak(navStep, TextToSpeech.QUEUE_FLUSH, null, null)
                                isNavigating = false
                            } else {
                                navStep = "Continúe por la ruta. Distancia: ${String.format("%.1f", distanceKm)} km."
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                kotlinx.coroutines.delay(10000) // update route every 10 seconds
            }
        } else {
            routePoints = emptyList()
        }
    }"""
content = re.sub(route_pattern, new_route, content, flags=re.DOTALL)


with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)
