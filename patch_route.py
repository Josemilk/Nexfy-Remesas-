import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

# Replace the simulation with real distance calculation and OSRM fetching
sim_pattern = re.compile(r'\s*// Active Navigation loop simulation for live turn-by-turn feedback.*?speedKmH = 0f\n\s*\}\n\s*\}', re.DOTALL)
replacement = """    // Real routing and navigation
    LaunchedEffect(isNavigating, transportMode, searchLocation, targetLocation) {
        if (isNavigating) {
            tts.value?.speak("Iniciando navegación", TextToSpeech.QUEUE_FLUSH, null, null)
            val destLat = searchLocation?.first ?: targetLocation?.first ?: return@LaunchedEffect
            val destLng = searchLocation?.second ?: targetLocation?.second ?: return@LaunchedEffect
            
            while (isNavigating) {
                try {
                    val mode = when (transportMode) {
                        "foot" -> "foot"
                        "bike" -> "bike"
                        else -> "driving"
                    }
                    val urlStr = "https://router.project-osrm.org/route/v1/$mode/$userLng,$userLat;$destLng,$destLat?overview=full&geometries=geojson"
                    val url = java.net.URL(urlStr)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
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
                                navStep = "Continúe por la ruta sugerida. Distancia restante: ${String.format("%.1f", distanceKm)} km."
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

content = sim_pattern.sub(replacement, content)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)
