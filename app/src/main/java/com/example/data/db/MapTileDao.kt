package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MapTile
import kotlinx.coroutines.flow.Flow

@Dao
interface MapTileDao {

    @Query("SELECT * FROM map_tiles ORDER BY zoom, x, y")
    fun getAllTiles(): Flow<List<MapTile>>

    @Query("SELECT COUNT(*) FROM map_tiles")
    fun getTileCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM map_tiles")
    fun getTotalStorageBytes(): Flow<Long>

    @Query("SELECT DISTINCT regionName FROM map_tiles ORDER BY regionName")
    fun getRegions(): Flow<List<String>>

    @Query("DELETE FROM map_tiles WHERE regionName = :regionName")
    suspend fun clearRegionTiles(regionName: String)

    @Query("SELECT * FROM map_tiles WHERE zoom = :zoom AND x = :x AND y = :y LIMIT 1")
    suspend fun getMapTile(zoom: Int, x: Int, y: Int): MapTile?

    @Query("SELECT * FROM map_tiles WHERE zoom = :zoom")
    fun getTilesForZoom(zoom: Int): Flow<List<MapTile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapTile(tile: MapTile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapTiles(tiles: List<MapTile>)

    @Query("DELETE FROM map_tiles")
    suspend fun clearAllTiles()
}
