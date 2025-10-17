package com.example.mytravelcompanion.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.mytravelcompanion.R
import com.google.android.gms.location.*

class JourneyService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "journey_channel"

        val channel = NotificationChannel(
            notificationChannelId,
            "Journey Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Tracking in corso")
            .setContentText("Il tuo percorso è in registrazione…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // ogni 3 secondi
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    val intent = Intent("LOCATION_UPDATE")
                    intent.putExtra("lat", location.latitude)
                    intent.putExtra("lng", location.longitude)
                    sendBroadcast(intent)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
