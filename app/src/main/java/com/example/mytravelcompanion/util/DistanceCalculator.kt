package com.example.mytravelcompanion.util

import com.example.mytravelcompanion.data.LatLngSerializable
import kotlin.math.*

object DistanceCalculator {

    // Raggio medio della Terra in metri
    private const val EARTH_RADIUS = 6371000.0

    // Calcola la distanza tra due coordinate GPS con formula dell’haversine
    fun distanceBetween(p1: LatLngSerializable, p2: LatLngSerializable): Double {
        val dLat = Math.toRadians(p2.lat - p1.lat)
        val dLon = Math.toRadians(p2.lng - p1.lng)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(p1.lat)) *
                cos(Math.toRadians(p2.lat)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS * c
    }

    // Somma le distanze tra tutti i punti consecutivi in una lista
    fun totalDistance(path: List<LatLngSerializable>): Double {
        var total = 0.0
        for (i in 0 until path.size - 1) {
            total += distanceBetween(path[i], path[i + 1])
        }
        return total
    }
}
