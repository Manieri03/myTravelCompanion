package com.example.mytravelcompanion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journeys")
data class Journey(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val start: Long,
    val end: Long? = null,
    val path: String? = null //lista coordinate in JSON
)