package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeliveryStatus {
    PENDING,
    DELIVERED
}

@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val phone: String,
    val amountUsd: Double,
    val amountCup: Double,
    val address: String,
    val identityNumber: String = "",
    val note: String = "",
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val date: String,
    val zone: String = "Zona Centro",
    val photoUri: String? = null,
    val latitude: Double = 23.1367,
    val longitude: Double = -82.3816
)
