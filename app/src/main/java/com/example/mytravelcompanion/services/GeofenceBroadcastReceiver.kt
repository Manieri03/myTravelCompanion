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
import com.example.mytravelcompanion.util.SharedPrefManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val transition = geofencingEvent.geofenceTransition
        val triggering = geofencingEvent.triggeringGeofences ?: emptyList()

        triggering.forEach { geofence ->
            val id = geofence.requestId
            val isInside = SharedPrefManager.isInside(context, id)

            when (transition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    if (!isInside) {
                        showNotification(context, id)
                        SharedPrefManager.setInside(context, id, true)
                    } else {
                        Log.d("GEOFENCE", "Ignoro ingresso per $id")
                    }
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    SharedPrefManager.setInside(context, id, false)
                    Log.d("GEOFENCE", "uscita da $id")
                }
            }
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
