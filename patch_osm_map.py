import re

with open("app/src/main/java/com/example/ui/screens/OsmMapUtils.kt", "r") as f:
    content = f.read()

# Add imports
imports = """import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.events.MapEventsReceiver"""
content = content.replace("import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay", "import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay\n" + imports)

# Add params
old_params = """    searchLocation: Pair<Double, Double>? = null,
    onMapClick: (Double, Double) -> Unit = { _, _ -> }
) {"""
new_params = """    searchLocation: Pair<Double, Double>? = null,
    customMarkers: List<Pair<Double, Double>> = emptyList(),
    onMapClick: (Double, Double) -> Unit = { _, _ -> },
    onMapLongClick: (Double, Double) -> Unit = { _, _ -> },
    onMapScrolled: () -> Unit = {}
) {"""
content = content.replace(old_params, new_params)

# Add MapEventsOverlay and MapListener
factory_end = """            mapViewRef = mapView
            mapView
        },"""
new_factory_end = """            val mapEventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    p?.let { onMapClick(it.latitude, it.longitude) }
                    return false
                }
                override fun longPressHelper(p: GeoPoint?): Boolean {
                    p?.let { onMapLongClick(it.latitude, it.longitude) }
                    return false
                }
            }
            mapView.overlays.add(MapEventsOverlay(mapEventsReceiver))
            
            mapView.addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    onMapScrolled()
                    return false
                }
                override fun onZoom(event: ZoomEvent?): Boolean = false
            })

            mapViewRef = mapView
            mapView
        },"""
content = content.replace(factory_end, new_factory_end)

# Add custom markers logic in update block
update_start = """                        // Overlays
            // Keep rotation, compass, mylocation, remove others
            view.overlays.removeAll { it is Marker || it is Polyline }"""
new_update_start = """                        // Overlays
            // Keep rotation, compass, mylocation, events, remove others
            view.overlays.removeAll { it is Marker || it is Polyline }
            
            customMarkers.forEach { markerCoords ->
                val marker = Marker(view)
                marker.position = GeoPoint(markerCoords.first, markerCoords.second)
                marker.title = "Marcador guardado"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                view.overlays.add(marker)
            }"""
content = content.replace(update_start, new_update_start)

with open("app/src/main/java/com/example/ui/screens/OsmMapUtils.kt", "w") as f:
    f.write(content)

