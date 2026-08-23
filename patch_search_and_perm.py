import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

# Add Nominatim search
geocoder_code = """                                            val geocoder = android.location.Geocoder(context, java.util.Locale("es", "CU"))
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
                                            }"""

new_geocoder_code = """                                            // Fallback to Nominatim OSM Search
                                            val urlStr = "https://nominatim.openstreetmap.org/search?q=${java.net.URLEncoder.encode(searchQuery, "UTF-8")}&format=json&limit=1"
                                            val url = java.net.URL(urlStr)
                                            val conn = url.openConnection() as java.net.HttpURLConnection
                                            conn.requestProperty("User-Agent", "NexFyApp")
                                            conn.connectTimeout = 5000
                                            conn.readTimeout = 5000
                                            if (conn.responseCode == 200) {
                                                val response = conn.inputStream.bufferedReader().use { it.readText() }
                                                val jsonArray = org.json.JSONArray(response)
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    if (jsonArray.length() > 0) {
                                                        val result = jsonArray.getJSONObject(0)
                                                        val lat = result.getString("lat").toDouble()
                                                        val lon = result.getString("lon").toDouble()
                                                        searchLocation = Pair(lat, lon)
                                                        recenterTrigger++
                                                        Toast.makeText(context, "Ubicación encontrada", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Ubicación no encontrada", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } else {
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    Toast.makeText(context, "Error en la búsqueda. Código: ${conn.responseCode}", Toast.LENGTH_SHORT).show()
                                                }
                                            }"""

content = content.replace(geocoder_code, new_geocoder_code)

# Add Permission handling
perm_state = """    val scope = rememberCoroutineScope()
    var showGpsPrompt by remember { androidx.compose.runtime.mutableStateOf(false) }"""

new_perm_state = """    val scope = rememberCoroutineScope()
    var showGpsPrompt by remember { androidx.compose.runtime.mutableStateOf(false) }
    
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!fineGranted && !coarseGranted) {
            Toast.makeText(context, "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
        }
    }
    
    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }"""

content = content.replace(perm_state, new_perm_state)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

