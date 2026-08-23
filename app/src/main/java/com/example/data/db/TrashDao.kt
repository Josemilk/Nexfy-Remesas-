package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TrashItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashDao {
    @Query("SELECT * FROM trash_items ORDER BY deletedAtTimestamp DESC")
    fun getAllTrashItems(): Flow<List<TrashItem>>

    @Query("SELECT * FROM trash_items ORDER BY deletedAtTimestamp DESC")
    suspend fun getAllTrashItemsDirect(): List<TrashItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrashItem(item: TrashItem): Long

    @Query("DELETE FROM trash_items WHERE id = :id")
    suspend fun deleteTrashItemById(id: Long)

    @Query("DELETE FROM trash_items WHERE id IN (:ids)")
    suspend fun deleteTrashItemsByIds(ids: List<Long>)

    @Query("DELETE FROM trash_items")
    suspend fun clearAllTrash()

    @Query("DELETE FROM trash_items WHERE (:currentTime - deletedAtTimestamp) > :maxAgeMillis")
    suspend fun purgeOldTrash(currentTime: Long, maxAgeMillis: Long = 2592000000L) // 30 days
}
