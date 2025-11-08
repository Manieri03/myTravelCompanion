package com.example.mytravelcompanion.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mytravelcompanion.R
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.util.SharedPrefManager
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class InactivityWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getDatabase(applicationContext).tripDao()
        val lastEndDateStr = dao.getLastCompletedTripEndDate() ?: return Result.success()
        val lastEndDate = LocalDate.parse(lastEndDateStr)
        val daysSince = ChronoUnit.DAYS.between(lastEndDate, LocalDate.now())

        //1 giorno per debug, 10 più corretto
        if (daysSince >= 1L) {
            showNotification()
        }

        return Result.success()
    }


    private fun showNotification() {
        if (SharedPrefManager.hasAlreadyNotified(applicationContext)) return

        createNotificationChannel()

        val builder = NotificationCompat.Builder(applicationContext, "trip_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Il tuo compagno di viaggio")
            .setContentText("È passato un po’ dall’ultimo viaggio, è ora di partire di nuovo!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(2001, builder.build())
            // Notifica già mostrata
            SharedPrefManager.markNotified(applicationContext)
        }
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
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}