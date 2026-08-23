package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val pinRequired: Boolean = true,
    val pinCode: String = "1234",
    val hiddenMode: Boolean = false,
    val usdCupRate: Double = 250.0,
    val commissionPercent: Double = 3.0,
    val whatsappMessage: String = "Hola, tu remesa está lista para recoger.",
    val autoBackup: Boolean = true,
    val darkMode: Boolean = false,
    val hideAmounts: Boolean = false,
    val offlineMapDownloaded: Boolean = true,
    val gpsHighPrecision: Boolean = true,
    val mapLayer: String = "TOPOGRAPHIC" // TOPOGRAPHIC or SATELLITE
)
