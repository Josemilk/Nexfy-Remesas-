package com.example.ui.screens

import android.util.Log

object ObfNativeEngine {
    private const val TAG = "ObfNativeEngine"

    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("osmand_native")
            isNativeLoaded = true
            Log.i(TAG, "Native C++ libosmand library loaded successfully. Version: ${getNativeVersion()}")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Native library osmand_native not found or failed to load. Falling back to Kotlin engine.", e)
            isNativeLoaded = false
        } catch (e: Exception) {
            Log.e(TAG, "Error loading osmand_native library", e)
            isNativeLoaded = false
        }
    }

    fun isNativeEngineAvailable(): Boolean = isNativeLoaded

    external fun getNativeVersion(): String
    external fun nativeInitObfReader(filePath: String): Boolean
    external fun nativeCalculateAStarRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        profile: String
    ): DoubleArray?

    fun calculateRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        profile: String = "car"
    ): List<Pair<Double, Double>> {
        if (isNativeLoaded) {
            try {
                val rawCoords = nativeCalculateAStarRoute(startLat, startLng, endLat, endLng, profile)
                if (rawCoords != null && rawCoords.size >= 4) {
                    val result = mutableListOf<Pair<Double, Double>>()
                    for (i in 0 until rawCoords.size - 1 step 2) {
                        result.add(Pair(rawCoords[i], rawCoords[i + 1]))
                    }
                    return result
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calling native C++ route engine", e)
            }
        }
        
        // Fallback to ObfMapEngine Kotlin implementation
        val (pts, _) = ObfMapEngine.calculateOfflineAStarRoute(startLat, startLng, endLat, endLng, profile)
        return pts
    }
}
