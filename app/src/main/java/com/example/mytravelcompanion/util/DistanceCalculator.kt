package com.example.mytravelcompanion.util

import com.example.mytravelcompanion.data.LatLngSerializable
import kotlin.math.*

object DistanceCalculator {

    private const val EARTH_RADIUS = 6371000.0

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

    fun totalDistance(path: List<LatLngSerializable>): Double {
        var total = 0.0
        for (i in 0 until path.size - 1) {
            total += distanceBetween(path[i], path[i + 1])
        }
        return total
    }

    fun distanceToSegment(p: LatLngSerializable, v: LatLngSerializable, w: LatLngSerializable): Double {
        val l2 = distanceBetween(v, w).pow(2)
        if (l2 == 0.0) return distanceBetween(p, v)
        val t = max(0.0, min(1.0,
            ((p.lat - v.lat) * (w.lat - v.lat) + (p.lng - v.lng) * (w.lng - v.lng)) / l2))
        val projection = LatLngSerializable(
            v.lat + t * (w.lat - v.lat),
            v.lng + t * (w.lng - v.lng)
        )
        return distanceBetween(p, projection)
    }
}