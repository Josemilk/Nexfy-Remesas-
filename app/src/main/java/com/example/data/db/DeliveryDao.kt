package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Delivery
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries ORDER BY id DESC")
    fun getAllDeliveries(): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries ORDER BY id DESC")
    suspend fun getAllDeliveriesDirect(): List<Delivery>

    @Query("SELECT * FROM deliveries WHERE id = :id")
    suspend fun getDeliveryById(id: Long): Delivery?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: Delivery): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveries(deliveries: List<Delivery>)

    @Update
    suspend fun updateDelivery(delivery: Delivery)

    @Query("DELETE FROM deliveries WHERE id = :id")
    suspend fun deleteDeliveryById(id: Long)

    @Query("DELETE FROM deliveries WHERE id IN (:ids)")
    suspend fun deleteDeliveriesByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM deliveries WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>
}
