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
        val segmentLength = distanceBetween(v, w)
        if (segmentLength < 1e-6) return distanceBetween(p, v)

        val midLat = (v.lat + w.lat) / 2.0
        val midLng = (v.lng + w.lng) / 2.0

        val metersPerDegreeLat = EARTH_RADIUS * Math.PI / 180.0
        val metersPerDegreeLng = metersPerDegreeLat * cos(Math.toRadians(midLat))
        fun toLocalMeters(point: LatLngSerializable): Pair<Double, Double> {
            val x = (point.lng - midLng) * metersPerDegreeLng
            val y = (point.lat - midLat) * metersPerDegreeLat
            return Pair(x, y)
        }

        val (px, py) = toLocalMeters(p)
        val (vx, vy) = toLocalMeters(v)
        val (wx, wy) = toLocalMeters(w)

        val dx = wx - vx
        val dy = wy - vy
        val l2 = dx * dx + dy * dy
        val t = max(0.0, min(1.0, ((px - vx) * dx + (py - vy) * dy) / l2))

        val projX = vx + t * dx
        val projY = vy + t * dy

        val distX = px - projX
        val distY = py - projY
        return sqrt(distX * distX + distY * distY)
    }
}