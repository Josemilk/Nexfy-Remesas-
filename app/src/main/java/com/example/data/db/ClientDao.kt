package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients ORDER BY name ASC")
    suspend fun getAllClientsDirect(): List<Client>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<Client>)

    @Update
    suspend fun updateClient(client: Client)
    
    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteClient(clientId: Long)

    @Query("DELETE FROM clients WHERE id IN (:clientIds)")
    suspend fun deleteClientsByIds(clientIds: List<Long>)
}
