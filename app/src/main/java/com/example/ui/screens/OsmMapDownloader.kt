package com.example.ui.screens

import android.content.Context
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView

object OsmMapDownloader {
    fun downloadMap(
        context: Context,
        layer: String,
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit
    ) {
        val tileSource = OsmMapUtils.getTileSource(layer)
        val mapView = MapView(context)
        mapView.setTileSource(tileSource)
        
        val cacheManager = CacheManager(mapView)
        
        // Bounding box for Cuba approximately
        val bb = BoundingBox(23.2, -74.0, 19.8, -85.0)
        
        val zoomMin = 10
        val zoomMax = 12
        
        cacheManager.downloadAreaAsyncNoUI(
            context,
            bb,
            zoomMin,
            zoomMax,
            object : CacheManager.CacheManagerCallback {
                private var totalTiles = 1
                
                override fun onTaskComplete() {
                    onProgress(1f)
                    onComplete()
                }

                override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) {
                    onProgress(progress.toFloat() / totalTiles)
                }

                override fun downloadStarted() {}

                override fun setPossibleTilesInArea(total: Int) {
                    totalTiles = total
                }
                
                override fun onTaskFailed(errors: Int) {
                    onComplete()
                }
            }
        )
    }
}
