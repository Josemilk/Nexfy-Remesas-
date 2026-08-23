with open("current_imports.txt", "r") as f:
    imports = f.read()

# Adding extra imports if needed
if "androidx.compose.material3.ExperimentalMaterial3Api" not in imports:
    imports += "import androidx.compose.material3.ExperimentalMaterial3Api\n"
if "androidx.compose.material3.ModalBottomSheet" not in imports:
    imports += "import androidx.compose.material3.ModalBottomSheet\n"
if "androidx.compose.material3.rememberModalBottomSheetState" not in imports:
    imports += "import androidx.compose.material3.rememberModalBottomSheetState\n"

# I will write the rest of the file
code = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMapScreen(
    targetLocation: Pair<Double, Double>?,
    deliveryId: Long?,
    isManualPin: Boolean = false,
    viewModel: NexFyViewModel,
    onBack: () -> Unit,
    onViewDetails: (Long) -> Unit
) {
    val context = LocalContext.current
    
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale("es", "ES")
            }
        }
        tts.value = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val deliveries by viewModel.deliveries.collectAsState()
    val delivery = deliveries.find { it.id == deliveryId } ?: deliveries.firstOrNull()
    val isMapDownloaded by viewModel.isMapDownloaded.collectAsState()
    val isDownloadingMap by viewModel.isDownloadingMap.collectAsState()
    val mapDownloadProgress by viewModel.mapDownloadProgress.collectAsState()
    val storedTileCount by viewModel.storedTileCount.collectAsState()
    val isGpsActive by viewModel.isGpsActive.collectAsState()
    val isGpsServiceRunning by viewModel.isGpsServiceRunning.collectAsState()
    val gpsSpeed by viewModel.gpsSpeed.collectAsState()
    val gpsAccuracy by viewModel.gpsAccuracy.collectAsState()
    val userLat by viewModel.userLatitude.collectAsState()
    val userLng by viewModel.userLongitude.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val downloadedRegions by viewModel.downloadedRegions.collectAsState()

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isNavigating by remember { mutableStateOf(false) }
    var navStep by remember { mutableStateOf("En 150m gire a la derecha en Calle 23") }
    var speedKmH by remember { mutableFloatStateOf(0f) }
    var manualMarkerMapOffset by remember { mutableStateOf<Offset?>(null) }
    var isManualPinMode by remember { mutableStateOf(isManualPin) }
    
    val mapType = settings.mapLayer
    var distanceKm by remember { mutableFloatStateOf(0f) }
    var etaMinutes by remember { mutableStateOf(0) }
    
    var showMapSettingsSheet by remember { mutableStateOf(false) }

    // Auto-prompt to enable location if not active
    LaunchedEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "Por favor active la ubicación para usar el mapa.", Toast.LENGTH_LONG).show()
        }
    }

    // Target Location initial centering
    LaunchedEffect(targetLocation) {
        if (targetLocation != null) {
            // we could calculate the offset based on lat/lng here
            // simplified: center to target location visually by resetting offset since we don't have a real map engine
            offsetX = 0f
            offsetY = 0f
            scale = 1.5f
            Toast.makeText(context, "Mostrando ubicación compartida", Toast.LENGTH_SHORT).show()
        }
    }

    // Real GPS Listener using LocationManager
    DisposableEffect(isGpsActive) {
        if (isGpsActive) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    viewModel.updateLocation(location.latitude, location.longitude)
                    speedKmH = if (location.hasSpeed()) location.speed * 3.6f else 28f
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (locationManager != null && (hasFine || hasCoarse)) {
                try {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, listener)
                    } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 1f, listener)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onDispose {
                locationManager?.removeUpdates(listener)
            }
        } else {
            onDispose { }
        }
    }

    // Active Navigation loop simulation for live turn-by-turn feedback
    LaunchedEffect(isNavigating) {
        if (isNavigating) {
            val steps = listOf(
                "En 150 metros gire a la derecha",
                "Siga recto 300 metros por la avenida",
                "Atención, zona de desvío. Recalculando ruta óptima...",
                "Nueva ruta sugerida. Gire a la izquierda en 50 metros",
                "En 80 metros gire a la izquierda hacia el destino",
                "¡Ha llegado al destino!"
            )
            tts.value?.speak("Iniciando ruta hacia el destino", TextToSpeech.QUEUE_FLUSH, null, null)
            var idx = 0
            while (isNavigating) {
                delay(4000)
                idx = (idx + 1) % steps.size
                navStep = steps[idx]
                speedKmH = if (idx == 2) 15f else 25f + (idx * 3)
                tts.value?.speak(navStep, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } else {
            speedKmH = 0f
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCBD5E1))
    ) {
        val mapWidth = constraints.maxWidth.toFloat()
        val mapHeight = constraints.maxHeight.toFloat()
        
        val baseLat = 23.1367
        val baseLng = -82.3584
        val dLat = ((userLat - baseLat) * 50000f).toFloat()
        val dLng = ((userLng - baseLng) * 50000f).toFloat()
        val userCenter = Offset(mapWidth * 0.25f + offsetX + dLng, mapHeight * 0.65f + offsetY - dLat)
        
        // Target calculation
        val clientCenter = when {
            targetLocation != null -> Offset(mapWidth * 0.5f + offsetX, mapHeight * 0.5f + offsetY)
            manualMarkerMapOffset != null -> Offset(manualMarkerMapOffset!!.x * scale + offsetX, manualMarkerMapOffset!!.y * scale + offsetY)
            else -> Offset(mapWidth * 0.78f + offsetX, mapHeight * 0.30f + offsetY)
        }
        
        val dx = clientCenter.x - userCenter.x
        val dy = clientCenter.y - userCenter.y
        val distancePixels = kotlin.math.sqrt((dx*dx + dy*dy).toDouble()).toFloat() / scale
        val newDistanceKm = distancePixels / 450f
        
        LaunchedEffect(newDistanceKm, speedKmH) {
            distanceKm = newDistanceKm
            etaMinutes = (newDistanceKm / (speedKmH.coerceAtLeast(15f)) * 60f).toInt()
        }
        
        // Wrap everything inside a standard Box
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Background / Map renderer
            OsmMapView(
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 4f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }.pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (isManualPinMode) {
                            manualMarkerMapOffset = Offset((offset.x - offsetX) / scale, (offset.y - offsetY) / scale)
                        }
                    }
                },
                mapType = settings.mapLayer,
                userLat = userLat,
                userLng = userLng,
                clientLat = targetLocation?.first ?: 23.1367,
                clientLng = targetLocation?.second ?: -82.3584,
                onMapClick = { lat, lng -> }
            )
            
            // Client / Target pin
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = 8.dp.toPx()
                drawCircle(
                    color = Color.Red,
                    radius = r,
                    center = clientCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = r * 0.4f,
                    center = clientCenter
                )
            }

            // Top Status Bar (Nav instructions)
            if (isNavigating) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = navStep,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Llegada en $etaMinutes min (${String.format("%.1f", distanceKm)} km)",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                // Top Search & Back
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FloatingActionButton(
                        onClick = onBack,
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E1B4B),
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    
                    Box(
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
                    }
                }
            }

            // Right side controls (Layers, Compass)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showMapSettingsSheet = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E1B4B),
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Capas y Ajustes")
                }
                
                FloatingActionButton(
                    onClick = { /* Compass reset */ offsetX = 0f; offsetY = 0f },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E1B4B),
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = "Brújula")
                }
            }

            // Bottom Right Controls (Center GPS, Zoom In, Zoom Out)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        scale = 1.0f
                        offsetX = 0f
                        offsetY = 0f
                        Toast.makeText(context, "Centrado en tu ubicación", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF2563EB),
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar GPS")
                }
                
                FloatingActionButton(
                    onClick = { scale = (scale + 0.5f).coerceAtMost(4f) },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E1B4B),
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }
                
                FloatingActionButton(
                    onClick = { scale = (scale - 0.5f).coerceAtLeast(0.5f) },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E1B4B),
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
            }

            // Bottom Left Controls (Menu, Directions)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showMapSettingsSheet = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E1B4B),
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                }
                
                FloatingActionButton(
                    onClick = { isNavigating = !isNavigating },
                    containerColor = if (isNavigating) Color(0xFFEF4444) else Color(0xFF2563EB),
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        if (isNavigating) Icons.Default.Close else Icons.Default.Directions, 
                        contentDescription = "Navegar"
                    )
                }
            }

            // Map Settings Bottom Sheet
            if (showMapSettingsSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showMapSettingsSheet = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Ajustes del Mapa Offline", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        
                        Text("Capa del Mapa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("Vectorial", "Satelital", "Topográfico").forEach { layer ->
                                Button(
                                    onClick = { viewModel.updateSettings(settings.copy(mapLayer = layer)) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (settings.mapLayer == layer) Color(0xFF2563EB) else Color.LightGray
                                    )
                                ) {
                                    Text(layer)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Descargar Mapa (Mosaicos Offline)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        
                        Button(
                            onClick = {
                                if (!isDownloadingMap) {
                                    viewModel.startMapDownload()
                                    Toast.makeText(context, "Iniciando descarga...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMapDownloaded) Color(0xFF059669) else Color(0xFF2563EB)
                            )
                        ) {
                            Icon(
                                imageVector = if (isMapDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isDownloadingMap -> "Descargando mosaicos... ${(mapDownloadProgress * 100).toInt()}%"
                                    isMapDownloaded -> "Actualizar Paquete de Mosaicos"
                                    else -> "Descargar mapa offline de Cuba"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isDownloadingMap || mapDownloadProgress > 0f) {
                            LinearProgressIndicator(
                                progress = { mapDownloadProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF2563EB)
                            )
                        }

                        Text("Servicio GPS en Segundo Plano", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Activo para seguimiento continuo", color = Color.Gray, fontSize = 14.sp)
                            androidx.compose.material3.Switch(
                                checked = isGpsServiceRunning,
                                onCheckedChange = { enabled ->
                                    if (enabled) viewModel.startGpsService() else viewModel.stopGpsService()
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write("package com.example.ui.screens\n")
    f.write(imports)
    f.write("\nimport androidx.compose.material.icons.filled.Menu\n")
    f.write("import androidx.compose.material.icons.filled.Directions\n")
    f.write("import androidx.compose.material.icons.filled.Close\n")
    f.write("import androidx.compose.foundation.rememberScrollState\n")
    f.write("import androidx.compose.foundation.verticalScroll\n")
    f.write(code)

