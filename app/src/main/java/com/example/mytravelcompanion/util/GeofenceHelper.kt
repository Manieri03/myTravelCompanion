import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.mytravelcompanion.data.Point
import com.example.mytravelcompanion.services.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceHelper {

    private const val GEOFENCE_RADIUS = 200f

    fun registerGeofences(context: Context, points: List<Point>) {
        val geofencingClient = LocationServices.getGeofencingClient(context)

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return

        val geofenceList = points.map {
            Geofence.Builder()
                .setRequestId(it.name)
                .setCircularRegion(it.latitude, it.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        }

        if (geofenceList.isEmpty()) return

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        geofencingClient.removeGeofences(points.map { it.name }).addOnCompleteListener {
            geofencingClient.addGeofences(
                GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofences(geofenceList)
                    .build(),
                pendingIntent
            )
        }
    }
}
