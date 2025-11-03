import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.mytravelcompanion.data.Point
import com.example.mytravelcompanion.services.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceHelper {

    private const val GEOFENCE_RADIUS = 200f
    private const val TAG = "GEOFENCE"

    fun registerGeofences(context: Context, points: List<Point>) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        Log.d(TAG, "Tentativo di registrare ${points.size} geofence")

        // Controllo permessi
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val hasBackgroundLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "Permessi: FINE=$hasFineLocation, BACKGROUND=$hasBackgroundLocation")

        if (!hasFineLocation) {
            Log.e(TAG, "Permesso ACCESS_FINE_LOCATION mancante! Interrompo registrazione")
            return
        }

        val geofenceList = points.map {
            Log.d(TAG, "Creazione geofence per ${it.name} a (${it.latitude}, ${it.longitude})")
            Geofence.Builder()
                .setRequestId(it.name)
                .setCircularRegion(it.latitude, it.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        }

        if (geofenceList.isEmpty()) {
            Log.d(TAG, "Lista geofence vuota, niente da registrare")
            return
        }

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(TAG, "Rimozione geofence esistenti con ID: ${points.map { it.name }}")
        geofencingClient.removeGeofences(points.map { it.name })
            .addOnCompleteListener {
                Log.d(TAG, "Rimozione geofence completata, aggiungo nuovi geofence")
                geofencingClient.addGeofences(
                    GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofences(geofenceList)
                        .build(),
                    pendingIntent
                ).addOnSuccessListener {
                    Log.d(TAG, "Geofence aggiunti con successo")
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Errore nell'aggiungere geofence", e)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Errore nella rimozione geofence", e)
            }
    }
}
