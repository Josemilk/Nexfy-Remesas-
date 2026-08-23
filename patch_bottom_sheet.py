with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "r") as f:
    content = f.read()

replacement = """                        Text("Capa del Mapa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        Text("Modo de Transporte (Rutas)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            val modes = listOf("Pie" to "foot", "Bici" to "bike", "Carro" to "car", "Bus" to "bus", "Tren" to "train")
                            modes.forEach { (label, value) ->
                                Button(
                                    onClick = { transportMode = value; Toast.makeText(context, "Modo de transporte cambiado a $label. Se recalculará la ruta.", Toast.LENGTH_SHORT).show() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (transportMode == value) Color(0xFF059669) else Color.LightGray
                                    )
                                ) {
                                    Text(label, fontSize = 12.sp)
                                }
                            }
                        }"""

content = content.replace("""                        Text("Capa del Mapa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        }""", replacement)

with open("app/src/main/java/com/example/ui/screens/OfflineMapScreen.kt", "w") as f:
    f.write(content)

