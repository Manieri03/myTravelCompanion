package com.example.mytravelcompanion.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mytravelcompanion.data.Trip


@Entity(
    tableName = "journeys",
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
data class Journey(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Int,
    val start: Long,
    val end: Long? = null,
    val path: String? = null,
    val distanceMeters: Double? = null,
    val durationSeconds: Long? = null
)
