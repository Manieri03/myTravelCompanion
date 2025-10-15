package com.example.mytravelcompanion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "markers")
data class Marker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val latitude: Double,
    val longitude: Double,
    val note: String? = null,
    val photoPath: String? = null
)
