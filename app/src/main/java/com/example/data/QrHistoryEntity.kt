package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_history")
data class QrHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val title: String,
    val type: String,
    val isGenerated: Boolean, // true = generated in app, false = scanned
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val upiAmount: String? = null,
    val upiVpa: String? = null,
    val colorHex: String? = null,
    val hasCustomLogo: Boolean = false
)
