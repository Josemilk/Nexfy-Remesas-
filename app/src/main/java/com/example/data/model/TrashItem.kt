package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TrashType(val displayName: String) {
    CLIENT("Ficha de Cliente"),
    DELIVERY("Entrega / Remesa")
}

@Entity(tableName = "trash_items")
data class TrashItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: TrashType,
    val originalId: Long,
    val title: String,
    val subtitle: String,
    val detailsJson: String,
    val clientName: String = "",
    val deletedAtTimestamp: Long = System.currentTimeMillis()
) {
    fun daysRemaining(): Int {
        val elapsedMillis = System.currentTimeMillis() - deletedAtTimestamp
        val elapsedDays = (elapsedMillis / (1000 * 60 * 60 * 24)).toInt()
        val remaining = 30 - elapsedDays
        return if (remaining < 0) 0 else remaining
    }
}
