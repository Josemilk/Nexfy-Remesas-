import re

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

# Add button to view saved markers in Top bar or Bottom sheet
old_menu_fab = """                FloatingActionButton(
                    onClick = { showMapSettingsSheet = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E1B4B),
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                }"""

new_menu_fab = """                FloatingActionButton(
                    onClick = { showSavedMarkersSheet = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF2563EB),
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Marcadores")
                }

                FloatingActionButton(
                    onClick = { showMapSettingsSheet = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E1B4B),
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                }"""

content = content.replace(old_menu_fab, new_menu_fab)

# Add showNavigationSetup dialog and showSavedMarkersSheet bottom sheet before showMapSettingsSheet
target = "if (showMapSettingsSheet) {"

dialogs_code = """            // Navigation Setup Dialog
            if (showNavigationSetup) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showNavigationSetup = false },
                    title = { Text("Configurar Navegación", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Origen (Inicio de ruta):", fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { navStartType = "Mi Ubicación" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (navStartType == "Mi Ubicación") Color(0xFF2563EB) else Color.LightGray
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) { Text("📍 Mi Ubicación", fontSize = 11.sp) }

                                Button(
                                    onClick = { navStartType = "Marcador" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (navStartType == "Marcador") Color(0xFF2563EB) else Color.LightGray
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) { Text("📌 Marcador", fontSize = 11.sp) }
                            }

                            if (navStartType == "Marcador") {
                                if (customMarkers.isEmpty()) {
                                    Text("No hay marcadores guardados. Mantén presionado el mapa para colocar uno.", color = Color.Red, fontSize = 12.sp)
                                } else {
                                    Text("Seleccionar Marcador de Origen:", fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        customMarkers.forEachIndexed { idx, _ ->
                                            OutlinedButton(
                                                onClick = { selectedStartMarkerIndex = idx },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (selectedStartMarkerIndex == idx) Color(0xFFDBEAFE) else Color.Transparent
                                                )
                                            ) { Text("#${idx + 1}") }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Destino (Punto final):", fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { navEndType = "Búsqueda" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (navEndType == "Búsqueda") Color(0xFF2563EB) else Color.LightGray
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) { Text("🔍 Búsqueda/Mapa", fontSize = 11.sp) }

                                Button(
                                    onClick = { navEndType = "Marcador" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (navEndType == "Marcador") Color(0xFF2563EB) else Color.LightGray
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) { Text("📌 Marcador", fontSize = 11.sp) }
                            }

                            if (navEndType == "Marcador") {
                                if (customMarkers.isEmpty()) {
                                    Text("No hay marcadores guardados. Mantén presionado el mapa para colocar uno.", color = Color.Red, fontSize = 12.sp)
                                } else {
                                    Text("Seleccionar Marcador de Destino:", fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        customMarkers.forEachIndexed { idx, _ ->
                                            OutlinedButton(
                                                onClick = { selectedEndMarkerIndex = idx },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (selectedEndMarkerIndex == idx) Color(0xFFDBEAFE) else Color.Transparent
                                                )
                                            ) { Text("#${idx + 1}") }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Modo de Transporte:", fontWeight = FontWeight.SemiBold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("car" to "🚗 Auto", "bike" to "🚴 Bici", "foot" to "🚶 Pie").forEach { (modeKey, modeLabel) ->
                                    Button(
                                        onClick = { transportMode = modeKey },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (transportMode == modeKey) Color(0xFF059669) else Color.LightGray
                                        )
                                    ) { Text(modeLabel, fontSize = 12.sp) }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val startCoord = if (navStartType == "Mi Ubicación") {
                                    if (userLat != 0.0 && userLng != 0.0) Pair(userLat, userLng) else Pair(23.1367, -82.3584)
                                } else {
                                    if (customMarkers.isNotEmpty()) customMarkers[selectedStartMarkerIndex.coerceIn(0, customMarkers.lastIndex)] else Pair(23.1367, -82.3584)
                                }

                                val endCoord = if (navEndType == "Búsqueda") {
                                    searchLocation ?: (targetLocation ?: Pair(23.1380, -82.3500))
                                } else {
                                    if (customMarkers.isNotEmpty()) customMarkers[selectedEndMarkerIndex.coerceIn(0, customMarkers.lastIndex)] else searchLocation ?: Pair(23.1380, -82.3500)
                                }

                                navStartLocation = startCoord
                                navEndLocation = endCoord
                                showNavigationSetup = false
                                isNavigating = true
                                recenterTrigger++
                                Toast.makeText(context, "Calculando ruta de navegación...", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Iniciar Navegación")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showNavigationSetup = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Saved Markers Bottom Sheet
            if (showSavedMarkersSheet) {
                ModalBottomSheet(onDismissRequest = { showSavedMarkersSheet = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Marcadores Personalizados Guardados (${customMarkers.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                        
                        if (customMarkers.isEmpty()) {
                            Text("No tienes marcadores guardados aún.", color = Color.Gray)
                            Text("💡 Consejo: Mantén presionado cualquier punto del mapa para guardar un marcador personalizado.", fontSize = 13.sp, color = Color(0xFF2563EB))
                        } else {
                            customMarkers.forEachIndexed { idx, marker ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Marcador #${idx + 1}", fontWeight = FontWeight.Bold)
                                            Text("Lat: ${String.format("%.5f", marker.first)}, Lng: ${String.format("%.5f", marker.second)}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Row {
                                            IconButton(onClick = {
                                                searchLocation = marker
                                                recenterTrigger++
                                                showSavedMarkersSheet = false
                                                Toast.makeText(context, "Centrado en Marcador #${idx + 1}", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.LocationOn, contentDescription = "Ir a marcador", tint = Color(0xFF2563EB))
                                            }
                                            IconButton(onClick = {
                                                customMarkers = customMarkers.filterIndexed { i, _ -> i != idx }
                                                Toast.makeText(context, "Marcador eliminado", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { customMarkers = emptyList(); Toast.makeText(context, "Todos los marcadores eliminados", Toast.LENGTH_SHORT).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Borrar todos los marcadores")
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            if (showMapSettingsSheet) {"""

content = content.replace(target, dialogs_code)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)
