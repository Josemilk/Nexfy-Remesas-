package com.example.data.model

import androidx.room.Entity

@Entity(tableName = "map_tiles", primaryKeys = ["zoom", "x", "y"])
data class MapTile(
    val zoom: Int,
    val x: Int,
    val y: Int,
    val tileKey: String,
    val regionName: String = "Cuba Vectorial",
    val tileType: String = "vector_data",
    val contentJson: String,
    val sizeBytes: Long = 1024L,
    val downloadedAt: Long = System.currentTimeMillis()
)
