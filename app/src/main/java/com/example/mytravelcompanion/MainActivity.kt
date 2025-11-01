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

        createNotificationChannel()
        scheduleInactivityCheck()


        setContent {
            MyTravelCompanionTheme {
                val context = LocalContext.current
                val dao = AppDatabase.getDatabase(context).tripDao()
                val markerDAO = AppDatabase.getDatabase(context).MarkerDAO()
                val journeyDAO = AppDatabase.getDatabase(context).JourneyDAO()
                val pointDAO= AppDatabase.getDatabase(context).PointDAO()

                val tripViewModel: TripViewModel = viewModel(
                    factory = TripViewModelFactory(dao, markerDAO, journeyDAO, pointDAO)
                )

                NavHost()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Trip Notifications"
            val descriptionText = "Notifiche sui viaggi"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("trip_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}