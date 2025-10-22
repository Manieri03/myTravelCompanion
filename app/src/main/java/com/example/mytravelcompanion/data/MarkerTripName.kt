package com.example.mytravelcompanion.data

data class MarkerWithTripName(
    val id: Long,
    val tripId: Int,
    val photoPath: String?,
    val destination: String
)