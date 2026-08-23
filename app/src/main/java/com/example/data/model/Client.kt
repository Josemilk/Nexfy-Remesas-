package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val zone: String,
    val address: String = "",
    val identityNumber: String = "",
    val totalDeliveredUsd: Double = 0.0,
    val lastDeliveryTime: String = ""
)
