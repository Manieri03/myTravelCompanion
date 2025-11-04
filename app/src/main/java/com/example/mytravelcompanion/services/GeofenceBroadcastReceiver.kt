package com.example.mytravelcompanion.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mytravelcompanion.R
import com.example.mytravelcompanion.data.AppDatabase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("GEOFENCE", "Extras: ${intent.extras?.keySet()}")
        Log.d("GEOFENCE", "Receiver chiamato! Intent: $intent")

        val geofenceIds = intent.getStringArrayListExtra("com.google.android.location.intent.extra.geofence")
        Log.d("GEOFENCE", "Fallback geofence ids: $geofenceIds")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.d("GEOFENCE", "GeofencingEvent null!")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.d("GEOFENCE", "Errore geofence: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d("GEOFENCE", "Transizione geofence: $geofenceTransition")

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            Log.d("GEOFENCE", "Geofence trigger: ${triggeringGeofences?.map { it.requestId }}")
            triggeringGeofences?.forEach { geofence ->
                showNotification(context, geofence.requestId)
            }
        } else {
            Log.d("GEOFENCE", "Transizione non gestita: $geofenceTransition")
        }
    }


    private fun showNotification(context: Context, pointName: String) {
        Log.d("GEOFENCE", "showNotification chiamato per $pointName")

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("GEOFENCE", "Permesso POST_NOTIFICATIONS non concesso!")
            return
        }

        val channelId = "poi_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Punti di interesse",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avvisi quando ti avvicini a un punto di interesse"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.point_interest_icon)
            .setContentTitle("Sei vicino a un punto di interesse!")
            .setContentText(pointName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(pointName.hashCode(), builder.build())
    }


}
