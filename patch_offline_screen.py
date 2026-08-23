import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

# 1. Update GPS Centering FAB color according to prompt requirements
old_gps_fab = """                FloatingActionButton(
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

new_gps_fab = """                val hasExactGps = (userLat != 0.0 && userLng != 0.0)
                val gpsBtnColor = when {
                    !hasExactGps -> Color.Transparent
                    !isMapCentered -> Color(0xFF2563EB)
                    else -> Color(0xFF2563EB)
                }
                val gpsIconColor = if (!hasExactGps) Color(0xFF2563EB) else Color.White

                FloatingActionButton(
                    onClick = {
                        searchLocation = null
                        isMapCentered = true
                        recenterTrigger++
                        Toast.makeText(context, if (hasExactGps) "Centrado en tu ubicación GPS" else "Obteniendo posición GPS...", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = gpsBtnColor,
                    contentColor = gpsIconColor,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = if (gpsBtnColor != Color.Transparent) 6.dp else 0.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar GPS")
                }"""

content = content.replace(old_gps_fab, new_gps_fab)

# 2. Add Saved Markers sheet / dialog variable
if 'var showSavedMarkersSheet by remember' not in content:
    content = content.replace(
        'var showNavigationSetup by remember { mutableStateOf(false) }',
        'var showNavigationSetup by remember { mutableStateOf(false) }\n    var showSavedMarkersSheet by remember { mutableStateOf(false) }\n    var selectedStartMarkerIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }\n    var selectedEndMarkerIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }'
    )

# 3. Add robust Route Calculation logic with offline fallback
old_route_effect = r'LaunchedEffect\(isNavigating, transportMode, navStartLocation, navEndLocation\).*?else \{\s*routePoints = emptyList\(\)\s*\}\s*\}'

new_route_effect = """LaunchedEffect(isNavigating, transportMode, navStartLocation, navEndLocation) {
        if (isNavigating && navStartLocation != null && navEndLocation != null) {
            tts.value?.speak("Iniciando navegación", TextToSpeech.QUEUE_FLUSH, null, null)
            val startLat = navStartLocation!!.first
            val startLng = navStartLocation!!.second
            val destLat = navEndLocation!!.first
            val destLng = navEndLocation!!.second
            
            while (isNavigating) {
                var routeFetched = false
                try {
                    val mode = when (transportMode) {
                        "foot" -> "foot"
                        "bike" -> "bike"
                        else -> "driving"
                    }
                    val currentLat = if (navStartType == "Mi Ubicación" && userLat != 0.0) userLat else startLat
                    val currentLng = if (navStartType == "Mi Ubicación" && userLng != 0.0) userLng else startLng
                    
                    val urlStr = "https://router.project-osrm.org/route/v1/$mode/$currentLng,$currentLat;$destLng,$destLat?overview=full&geometries=geojson"
                    val url = java.net.URL(urlStr)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("User-Agent", "NexFyApp")
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    
                    if (conn.responseCode == 200) {
                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = org.json.JSONObject(response)
                        val routes = json.optJSONArray("routes")
                        if (routes != null && routes.length() > 0) {
                            val route = routes.getJSONObject(0)
                            val distance = route.optDouble("distance") // in meters
                            val duration = route.optDouble("duration") // in seconds
                            
                            distanceKm = (distance / 1000.0).toFloat()
                            etaMinutes = (duration / 60.0).toInt().coerceAtLeast(1)
                            
                            val geometry = route.getJSONObject("geometry")
                            val coordinates = geometry.getJSONArray("coordinates")
                            val points = mutableListOf<Pair<Double, Double>>()
                            for (i in 0 until coordinates.length()) {
                                val point = coordinates.getJSONArray(i)
                                points.add(Pair(point.getDouble(1), point.getDouble(0)))
                            }
                            routePoints = points
                            routeFetched = true
                            
                            if (distance < 30) {
                                navStep = "¡Ha llegado al destino!"
                                tts.value?.speak(navStep, TextToSpeech.QUEUE_FLUSH, null, null)
                                isNavigating = false
                            } else {
                                navStep = "En ruta hacia el destino (${String.format("%.1f", distanceKm)} km)."
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Fallback offline route calculation if network failed
                if (!routeFetched) {
                    val currentLat = if (navStartType == "Mi Ubicación" && userLat != 0.0) userLat else startLat
                    val currentLng = if (navStartType == "Mi Ubicación" && userLng != 0.0) userLng else startLng
                    
                    val R = 6371.0 // Earth radius in km
                    val dLat = Math.toRadians(destLat - currentLat)
                    val dLng = Math.toRadians(destLng - currentLng)
                    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                            Math.cos(Math.toRadians(currentLat)) * Math.cos(Math.toRadians(destLat)) *
                            Math.sin(dLng / 2) * Math.sin(dLng / 2)
                    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                    distanceKm = (R * c).toFloat()

                    val speedKmH = when (transportMode) {
                        "foot" -> 5.0
                        "bike" -> 15.0
                        else -> 35.0
                    }
                    etaMinutes = ((distanceKm / speedKmH) * 60).toInt().coerceAtLeast(1)

                    // Generate 20 points line for map
                    val points = mutableListOf<Pair<Double, Double>>()
                    val steps = 20
                    for (i in 0..steps) {
                        val fraction = i.toDouble() / steps
                        val pLat = currentLat + (destLat - currentLat) * fraction
                        val pLng = currentLng + (destLng - currentLng) * fraction
                        points.add(Pair(pLat, pLng))
                    }
                    routePoints = points
                    navStep = "Navegación Offline (${String.format("%.1f", distanceKm)} km)."
                }

                kotlinx.coroutines.delay(10000) // update route every 10 seconds
            }
        } else {
            routePoints = emptyList()
        }
    }"""

content = re.sub(old_route_effect, new_route_effect, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

