package com.example.mytravelcompanion

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.data.TripViewModelFactory
import com.example.mytravelcompanion.navigation.NavHost
import com.example.mytravelcompanion.ui.theme.MyTravelCompanionTheme
import com.example.mytravelcompanion.workers.InactivityWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    102
                )
            }
        }

        createNotificationChannels()
        scheduleInactivityCheck()


        setContent {
            MyTravelCompanionTheme {
                val context = LocalContext.current
                val db = AppDatabase.getDatabase(context)
                val dao = db.tripDao()
                val markerDAO = db.MarkerDAO()
                val journeyDAO = db.JourneyDAO()
                val pointDAO = db.PointDAO()

                val tripViewModel: TripViewModel = viewModel(
                    factory = TripViewModelFactory(dao, markerDAO, journeyDAO, pointDAO)
                )

                NavHost(tripViewModel)
            }
        }
    }

    private fun scheduleInactivityCheck() {
        val workRequest = PeriodicWorkRequestBuilder<InactivityWorker>(12, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "trip_inactivity_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)

            //canale notifiche di viaggio
            val tripChannel = NotificationChannel(
                "trip_channel",
                "Notifiche di viaggio",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Promemoria e aggiornamenti sui tuoi viaggi"
            }

            //notifiche punti di interesse
            val poiChannel = NotificationChannel(
                "poi_channel",
                "Punti di interesse",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avvisi quando ti avvicini a un punto di interesse"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(tripChannel)
            notificationManager.createNotificationChannel(poiChannel)
        }
    }

}