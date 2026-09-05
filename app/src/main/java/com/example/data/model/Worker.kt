package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String = "Entregador",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val deviceId: String = "",
    val isLinked: Boolean = false,
    val statusText: String = "Disponible"
)
