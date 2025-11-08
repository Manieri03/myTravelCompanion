package com.example.mytravelcompanion.util

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.mytravelcompanion.data.Point
import com.example.mytravelcompanion.services.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceHelper {

    //Tag per debug
    private const val TAG = "GEOFENCE"

    //Lunghezza del raggio attorno al punto di interesse
    private const val GEOFENCE_RADIUS_METERS = 200f

    private fun getGeofencingClient(context: Context): GeofencingClient {
        return LocationServices.getGeofencingClient(context)
    }

    private fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    //registrazione di tutti i punti presenti sul db
    fun registerAll(context: Context, points: List<Point>) {
        if (points.isEmpty()) {
            Log.d(TAG, "Nessun punto da registrare")
            return
        }

        if (!checkPermissions(context)) {
            Log.e(TAG, "Permessi mancanti per registrare geofence")
            return
        }

        val geofenceList = points.map { point ->
            Geofence.Builder()
                .setRequestId(point.name)
                .setCircularRegion(
                    point.latitude,
                    point.longitude,
                    GEOFENCE_RADIUS_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setLoiteringDelay(5000)
                .build()
        }

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        val client = getGeofencingClient(context)
        val pendingIntent = getGeofencePendingIntent(context)

        try {
            client.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Registrati ${points.size} geofence con successo")
                    points.forEach { point ->
                        Log.d(TAG, "  - ${point.name} (${point.latitude}, ${point.longitude})")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Errore registrazione geofence: ${e.message}", e)
                }

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}", e)
        }
    }

    //registrazione di un nuovo punto
    fun registerOne(context: Context, point: Point) {
        registerAll(context, listOf(point))
    }

    //rimozione di un geofence
    fun removeOne(context: Context, point: Point) {
        val client = getGeofencingClient(context)
        client.removeGeofences(listOf(point.name))
            .addOnSuccessListener {
                Log.d(TAG, "Geofence rimosso: ${point.name}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Errore rimozione geofence: ${e.message}", e)
            }
    }

    private fun checkPermissions(context: Context): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        Log.d(TAG, "Permessi: Fine: $fineLocation, Background: $backgroundLocation")
        return fineLocation && backgroundLocation
    }
}