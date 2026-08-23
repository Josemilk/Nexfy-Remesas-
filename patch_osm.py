import re

with open("app/src/main/java/com/example/ui/screens/OsmMapUtils.kt", "r") as f:
    content = f.read()

replacement = """@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    mapType: String,
    userLat: Double,
    userLng: Double,
    clientLat: Double,
    clientLng: Double,
    isNavigating: Boolean = false,
    routePoints: List<Pair<Double, Double>> = emptyList(),
    headingUp: Boolean = false,
    recenterTrigger: Int = 0,
    searchLocation: Pair<Double, Double>? = null,
    onMapClick: (Double, Double) -> Unit = { _, _ -> }
) {
    var mapViewRef by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<MapView?>(null) }
    
    androidx.compose.runtime.LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0) {
            mapViewRef?.let { view ->
                if (searchLocation != null) {
                    view.controller.animateTo(GeoPoint(searchLocation.first, searchLocation.second))
                } else {
                    view.controller.animateTo(GeoPoint(userLat, userLng))
                }
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            OsmMapUtils.initOsm(context)
            val mapView = MapView(context).apply {
                setMultiTouchControls(true)
                controller.setZoom(16.0)
                controller.setCenter(GeoPoint(userLat, userLng))
            }
            
            // Rotation gestures
            val rotationGestureOverlay = RotationGestureOverlay(mapView)
            rotationGestureOverlay.isEnabled = true
            mapView.overlays.add(rotationGestureOverlay)
            
            // Compass
            val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
            compassOverlay.enableCompass()
            mapView.overlays.add(compassOverlay)

            // My Location
            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            locationOverlay.enableMyLocation()
            mapView.overlays.add(locationOverlay)

            mapViewRef = mapView
            mapView
        },
        update = { view ->
            val tileSource = OsmMapUtils.getTileSource(mapType)
            if (view.tileProvider.tileSource != tileSource) {
                view.setTileSource(tileSource)
            }
            
            // Orient map
            val compass = view.overlays.filterIsInstance<CompassOverlay>().firstOrNull()
            if (headingUp && compass != null) {
                view.mapOrientation = 360 - compass.orientation
                view.controller.setCenter(GeoPoint(userLat, userLng))
            } else {
                if (view.mapOrientation != 0f) {
                    view.mapOrientation = 0f
                }
            }
            
            // Overlays
            // Keep rotation, compass, mylocation, remove others
            view.overlays.removeAll { it is Marker || it is Polyline }
            
            if (!isNavigating) {
                val clientMarker = Marker(view)
                clientMarker.position = GeoPoint(clientLat, clientLng)
                clientMarker.title = "Destino"
                clientMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                view.overlays.add(clientMarker)
            }
            
            if (routePoints.isNotEmpty()) {
                val polyline = Polyline(view)
                val geoPoints = routePoints.map { GeoPoint(it.first, it.second) }
                polyline.setPoints(geoPoints)
                polyline.outlinePaint.color = android.graphics.Color.BLUE
                polyline.outlinePaint.strokeWidth = 10f
                view.overlays.add(0, polyline) // add below markers
            }
            
            view.invalidate()
        }
    )
}"""

content = re.sub(r'@Composable\s*fun OsmMapView\(.*?\}\s*\)\s*\}', replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/OsmMapUtils.kt", "w") as f:
    f.write(content)

