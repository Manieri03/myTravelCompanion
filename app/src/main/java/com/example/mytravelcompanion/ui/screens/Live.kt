package com.example.mytravelcompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytravelcompanion.R
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.data.TripViewModelFactory
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.ui.theme.myTipography2
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import java.time.LocalDate
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.awaitCancellation

@Composable
fun Live() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).tripDao()
    val tripViewModel: TripViewModel = viewModel(
        factory = TripViewModelFactory(dao)
    )

    val trips by tripViewModel.trips.collectAsState(initial = emptyList())
    val currentTrip = tripViewModel.getCurrentTrip(trips)

    val launcher = rememberLauncherForActivityResult(RequestPermission()) { isGranted: Boolean ->
        //
    }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Se non concesso, lo chiediamo
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 18.dp, end = 18.dp, top = 55.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(45.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tc_logo),
                contentDescription = "Logo app",
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Viaggio in corso",
                fontSize = 26.sp,
                style = myTipography2.titleLarge
            )
        }

        // Mostra la mappa solo se c'è un viaggio e se il permesso è concesso
        if (currentTrip != null) {
            if (hasLocationPermission) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                ) {
                    TripMap(tripViewModel = tripViewModel)
                }
            } else {
                Text(
                    "Permesso posizione non concesso",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 18.sp
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Nessun viaggio",
                    fontSize = 26.sp,
                    style = myTipography2.titleLarge
                )
            }
        }



    }
}

@SuppressLint("MissingPermission")
@Composable
fun TripMap(
    tripViewModel: TripViewModel,
    startLatLng: LatLng = LatLng(41.9028, 12.4964), // Roma di default
    zoom: Float = 15f
) {
    var userLocation by remember { mutableStateOf(tripViewModel.lastKnownLocation) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLatLng, zoom)
    }

    //aggiornamento posizione live
    LaunchedEffect(Unit) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // ogni 3 secondi
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location? = result.lastLocation
                if (location != null) {
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    userLocation = newLatLng
                    tripViewModel.lastKnownLocation = newLatLng
                    // Aggiorna la camera per seguire la posizione
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(newLatLng, 16f)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )

        try {
            //attivo finche composable in vita
            awaitCancellation()
        } finally {
            // Quando il composable viene distrutto, ferma gli aggiornamenti GPS
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    if (userLocation == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Recupero posizione...",
                fontSize = 18.sp
            )
        }
    } else {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true
            )
        )
    }
}
