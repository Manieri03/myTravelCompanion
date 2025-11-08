package com.example.mytravelcompanion.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mytravelcompanion.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GEOFENCE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "OnReceive chiamato")
        Log.d(TAG, "Action: ${intent.action}")

        val event = GeofencingEvent.fromIntent(intent)

        if (event == null) {
            Log.e(TAG, "GeofencingEvent è NULL!")
            return
        }

        if (event.hasError()) {
            val errorMessage = getErrorString(event.errorCode)
            Log.e(TAG, "Errore geofence: $errorMessage (code: ${event.errorCode})")
            return
        }

        val transition = event.geofenceTransition
        Log.d(TAG, "Transition type: $transition")

        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d(TAG, "ENTER detected!")
                val triggered = event.triggeringGeofences

                if (triggered.isNullOrEmpty()) {
                    Log.w(TAG, "Nessun geofence triggering trovato")
                    return
                }

                Log.d(TAG, "Geofence triggerati: ${triggered.size}")
                triggered.forEach { geo ->
                    val id = geo.requestId
                    Log.d(TAG, "  -> Geofence ID: $id")
                    notifyPoi(context, id)
                }
            }
            else -> {
                Log.w(TAG, "Transition type sconosciuto: $transition")
            }
        }
    }

    private fun notifyPoi(context: Context, pointName: String) {
        Log.d(TAG, "Creazione notifica per: $pointName")

        val builder = NotificationCompat.Builder(context, "poi_channel")
            .setSmallIcon(R.drawable.tc_logo)
            .setContentTitle("Sei vicino ad un punto di interesse!")
            .setContentText(pointName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        val nm = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            nm.notify(pointName.hashCode(), builder.build())
            Log.d(TAG, "Notifica inviata con ID: ${pointName.hashCode()}")
        } else {
            Log.e(TAG, "Permesso POST_NOTIFICATIONS mancante!")
        }
    }

    private fun getErrorString(errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                "Geofence non disponibile"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                "Troppi geofence registrati"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                "Troppi PendingIntent"
            GeofenceStatusCodes.GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION ->
                "Permessi di localizzazione insufficienti"
            else -> "Errore sconosciuto: $errorCode"
        }
    }
}