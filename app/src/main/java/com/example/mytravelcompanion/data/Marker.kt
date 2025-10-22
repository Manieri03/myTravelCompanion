package com.example.mytravelcompanion.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mytravelcompanion.data.Trip

@Entity(
    tableName = "markers",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"])]
)
data class Marker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val latitude: Double,
    val longitude: Double,
    val note: String? = null,
    val photoPath: String? = null
)
