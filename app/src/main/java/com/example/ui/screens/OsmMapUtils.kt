package com.example.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.events.MapEventsReceiver

object OsmMapUtils {
    fun initOsm(context: Context) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    fun getTileSource(layerName: String) = when (layerName) {
        "SATELLITE", "Satelital" -> TileSourceFactory.USGS_SAT
        "HYBRID", "Híbrido" -> TileSourceFactory.USGS_SAT
        "TOPOGRAPHIC", "Topográfico", "Topo" -> TileSourceFactory.OpenTopo
        "NAUTICAL", "Náutico" -> TileSourceFactory.MAPNIK
        "WINTER", "Invernal" -> TileSourceFactory.MAPNIK
        else -> TileSourceFactory.MAPNIK
    }

    fun calculateBearing(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
        val lat1 = Math.toRadians(startLat)
        val lat2 = Math.toRadians(endLat)
        val dLng = Math.toRadians(endLng - startLng)
        val y = Math.sin(dLng) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng)
        val bearing = Math.toDegrees(Math.atan2(y, x))
        return ((bearing + 360) % 360).toFloat()
    }

    fun getGreenUserLocationIcon(context: Context): android.graphics.drawable.BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val size = (48 * density).toInt()
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

        val cx = size / 2f
        val cy = size / 2f

        // Outer translucent green pulse ring
        paint.color = android.graphics.Color.parseColor("#3310B981")
        canvas.drawCircle(cx, cy, size * 0.48f, paint)

        // Inner vibrant green circle
        paint.color = android.graphics.Color.parseColor("#10B981")
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawCircle(cx, cy, size * 0.35f, paint)

        // White border ring
        paint.color = android.graphics.Color.WHITE
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 3f * density
        canvas.drawCircle(cx, cy, size * 0.35f, paint)

        // White directional arrow inside pointing up (0 degrees)
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.WHITE
        val path = android.graphics.Path()
        val r = size * 0.22f
        path.moveTo(cx, cy - r)              // Top arrow tip
        path.lineTo(cx - r * 0.65f, cy + r * 0.65f) // Bottom left corner
        path.lineTo(cx, cy + r * 0.25f)       // Inner notch
        path.lineTo(cx + r * 0.65f, cy + r * 0.65f) // Bottom right corner
        path.close()
        canvas.drawPath(path, paint)

        return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
    }
}

data class CustomUserMarker(
    val name: String,
    val lat: Double,
    val lng: Double
)

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    mapType: String,
    userLat: Double,
    userLng: Double,
    clientLat: Double,
    clientLng: Double,
    isNavigating: Boolean = false,
    routePoints: List<Pair<Double, Double>> = emptyList(),
    gpxTrackPoints: List<Pair<Double, Double>> = emptyList(),
    measurePoints: List<Pair<Double, Double>> = emptyList(),
    headingUp: Boolean = false,
    recenterTrigger: Int = 0,
    zoomInTrigger: Int = 0,
    zoomOutTrigger: Int = 0,
    searchLocation: Pair<Double, Double>? = null,
    customMarkers: List<CustomUserMarker> = emptyList(),
    offlinePois: List<OfflinePoi> = emptyList(),
    showPoisOverlay: Boolean = false,
    onMapClick: (Double, Double) -> Unit = { _, _ -> },
    onMapLongClick: (Double, Double) -> Unit = { _, _ -> },
    onMapScrolled: () -> Unit = {}
) {
    var mapViewRef by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<MapView?>(null) }
    var prevPos by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Pair<Double, Double>?>(null) }
    var currentBearing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }

    androidx.compose.runtime.LaunchedEffect(userLat, userLng) {
        val prev = prevPos
        if (prev != null && (prev.first != userLat || prev.second != userLng)) {
            val dist = Math.hypot(userLat - prev.first, userLng - prev.second)
            if (dist > 0.00001) {
                currentBearing = OsmMapUtils.calculateBearing(prev.first, prev.second, userLat, userLng)
            }
        }
        prevPos = Pair(userLat, userLng)
    }
    
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

    androidx.compose.runtime.LaunchedEffect(zoomInTrigger) {
        if (zoomInTrigger > 0) {
            mapViewRef?.let { view ->
                view.controller.zoomIn()
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(zoomOutTrigger) {
        if (zoomOutTrigger > 0) {
            mapViewRef?.let { view ->
                view.controller.zoomOut()
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

            val mapEventsReceiver = object : MapEventsReceiver {
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
            // Keep rotation, compass, remove others
            view.overlays.removeAll { it is Marker || it is Polyline }

            // Green User Location Marker
            if (userLat != 0.0 && userLng != 0.0) {
                val userMarker = Marker(view)
                userMarker.position = GeoPoint(userLat, userLng)
                userMarker.title = "Tu Ubicación"
                userMarker.snippet = "Avanzando hacia el destino"
                userMarker.icon = OsmMapUtils.getGreenUserLocationIcon(view.context)
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                userMarker.rotation = currentBearing
                view.overlays.add(userMarker)
            }
            
            // Add custom markers (created and named manually by user)
            customMarkers.forEachIndexed { index, userMarker ->
                val customMarker = Marker(view)
                customMarker.position = GeoPoint(userMarker.lat, userMarker.lng)
                customMarker.title = userMarker.name.ifEmpty { "Marcador #${index + 1}" }
                customMarker.snippet = "Lat: ${String.format("%.4f", userMarker.lat)}, Lng: ${String.format("%.4f", userMarker.lng)}"
                customMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                view.overlays.add(customMarker)
            }

            // Search / Target Marker (only if user searched or clicked a location)
            if (searchLocation != null) {
                val searchMarker = Marker(view)
                searchMarker.position = GeoPoint(searchLocation.first, searchLocation.second)
                searchMarker.title = "Ubicación seleccionada"
                searchMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                view.overlays.add(searchMarker)
            }
            
            // GPX Recorded Track Polyline
            if (gpxTrackPoints.isNotEmpty()) {
                val gpxPolyline = Polyline(view)
                val gpxGeoPoints = gpxTrackPoints.map { GeoPoint(it.first, it.second) }
                gpxPolyline.setPoints(gpxGeoPoints)
                gpxPolyline.outlinePaint.color = android.graphics.Color.parseColor("#10B981") // Green GPX line
                gpxPolyline.outlinePaint.strokeWidth = 10f
                view.overlays.add(0, gpxPolyline)
            }

            // Measurement Tool Line
            if (measurePoints.isNotEmpty()) {
                val measurePolyline = Polyline(view)
                val mGeoPoints = measurePoints.map { GeoPoint(it.first, it.second) }
                measurePolyline.setPoints(mGeoPoints)
                measurePolyline.outlinePaint.color = android.graphics.Color.parseColor("#F59E0B") // Amber line
                measurePolyline.outlinePaint.strokeWidth = 8f
                measurePolyline.outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(20f, 10f), 0f)
                view.overlays.add(0, measurePolyline)

                measurePoints.forEachIndexed { idx, pt ->
                    val mMarker = Marker(view)
                    mMarker.position = GeoPoint(pt.first, pt.second)
                    mMarker.title = "Punto Medición #${idx + 1}"
                    mMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    view.overlays.add(mMarker)
                }
            }

            // Offline POIs Overlay
            if (showPoisOverlay && offlinePois.isNotEmpty()) {
                offlinePois.take(15).forEach { poi ->
                    val poiMarker = Marker(view)
                    poiMarker.position = GeoPoint(poi.lat, poi.lng)
                    poiMarker.title = poi.name
                    poiMarker.snippet = "${poi.category} • ${poi.address}"
                    poiMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    view.overlays.add(poiMarker)
                }
            }

            // Route polyline
            if (routePoints.isNotEmpty()) {
                val polyline = Polyline(view)
                val geoPoints = routePoints.map { GeoPoint(it.first, it.second) }
                polyline.setPoints(geoPoints)
                polyline.outlinePaint.color = android.graphics.Color.parseColor("#2563EB")
                polyline.outlinePaint.strokeWidth = 12f
                view.overlays.add(0, polyline) // add below markers

                // Start Marker
                val startMarker = Marker(view)
                startMarker.position = geoPoints.first()
                startMarker.title = "Inicio de Ruta"
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                view.overlays.add(startMarker)

                // End Marker
                val endMarker = Marker(view)
                endMarker.position = geoPoints.last()
                endMarker.title = "Destino Final"
                endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                view.overlays.add(endMarker)
            }
            
            view.invalidate()
        }
    )
}
