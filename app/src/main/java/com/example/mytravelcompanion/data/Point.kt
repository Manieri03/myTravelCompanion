package com.example.mytravelcompanion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "points")
data class Point(
    @PrimaryKey val name: String,
    val latitude: Double,
    val longitude: Double
)
