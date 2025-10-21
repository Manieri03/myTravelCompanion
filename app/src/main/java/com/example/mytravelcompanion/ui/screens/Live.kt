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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import com.example.mytravelcompanion.data.MarkerDAO
import com.example.mytravelcompanion.data.Trip
import com.example.mytravelcompanion.data.TripType
import com.example.mytravelcompanion.service.JourneyService
import com.example.mytravelcompanion.ui.theme.blu
import com.example.mytravelcompanion.ui.theme.ciano
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun Live() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).tripDao()
    val markerDAO= AppDatabase.getDatabase(context).MarkerDAO()
    val journeyDAO= AppDatabase.getDatabase(context).JourneyDAO()
    val tripViewModel: TripViewModel = viewModel(
        factory = TripViewModelFactory(dao, markerDAO, journeyDAO)
    )

    val trips by tripViewModel.trips.collectAsState(initial = emptyList())
    val currentTrip = tripViewModel.getCurrentTrip(trips)

    val isJourneyActive by tripViewModel.isJourneyActive.collectAsState()
    val serviceIntent = Intent(context, JourneyService::class.java)

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        TripMap(
                            tripViewModel = tripViewModel,
                            currentTrip = currentTrip,
                            isJourneyActive = isJourneyActive,
                            tripId = currentTrip.id.toLong()
                        )
                    }

                    Button(onClick = {
                        if (!isJourneyActive) {
                            if (hasLocationPermission) {
                                // Avvio percorso e service
                                currentTrip.id.toLong().let { tripViewModel.startJourney(it) }
                                serviceIntent.putExtra("tripId", currentTrip.id)
                                ContextCompat.startForegroundService(context, serviceIntent)
                            } else {
                                // Richiedi permesso
                                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        } else {
                            // Ferma percorso e poi service
                            tripViewModel.stopJourney()
                            context.stopService(serviceIntent)
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = ciano)
                    ) {
                        Text(if (!isJourneyActive) "Inizia percorso" else "Stop percorso", style = myTipography2.bodyLarge)
                    }

                    Button(
                        onClick = {
                            currentTrip?.let {
                                tripViewModel.markTripAsCompleted(it.id)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = blu)
                    ) {
                        Text("Concludi viaggio", style = myTipography2.bodyLarge)
                    }
                }
            } else {
                Text(
                    "Permesso posizione non concesso",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 18.sp
                )
            }
        }
         else {
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
    currentTrip: Trip? = null,
    zoom: Float = 15f,
    tripId: Long,
    isJourneyActive:Boolean
) {
    var userLocation by remember { mutableStateOf(tripViewModel.lastKnownLocation) }

    val markers = remember { mutableStateListOf<com.example.mytravelcompanion.data.Marker>() }

    // Dialog management
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showMainDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var currentNote by remember { mutableStateOf("") }
    var currentPhotoPath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }


    var selectedMarker by remember { mutableStateOf<com.example.mytravelcompanion.data.Marker?>(null) }
    var showMarkerDialog by remember { mutableStateOf(false) }

    var startLatLng by remember { mutableStateOf<LatLng?>(null) }
    val journeyPoints by tripViewModel.journeyPoints.collectAsState()

    // Recupera la posizione attuale al primo avvio
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                startLatLng = LatLng(it.latitude, it.longitude)
                tripViewModel.lastKnownLocation = startLatLng // opzionale, se vuoi mantenerla nel ViewModel
                android.util.Log.d("TripMap", "Posizione iniziale: ${it.latitude}, ${it.longitude}")
            }
        }
    }
    if (startLatLng == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Recupero posizione iniziale...", style = myTipography2.bodyLarge)
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLatLng!!, zoom)
    }


    // Camera / gallery launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output ->
                inputStream?.copyTo(output)
            }
            inputStream?.close()

            currentPhotoPath = file.absolutePath
        }
        showMainDialog = true
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val file = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            currentPhotoPath = file.absolutePath
        }
        showMainDialog = true
    }

    // Aggiornamento posizione utente
    LaunchedEffect(isJourneyActive) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val newLatLng = LatLng(loc.latitude, loc.longitude)
                userLocation = newLatLng
                tripViewModel.lastKnownLocation = newLatLng

                android.util.Log.d("TripMap", "Nuova posizione ricevuta: ${loc.latitude}, ${loc.longitude}")

                if (isJourneyActive) {
                    android.util.Log.d("TripMap", "Invio posizione al ViewModel per il percorso attivo")
                    tripViewModel.updateJourneyLocation(loc.latitude, loc.longitude)
                } else {
                    android.util.Log.d("TripMap", "Journey NON attivo")
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, callback, context.mainLooper)
        try {
            awaitCancellation()
        } finally {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }


    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val lat = it.getDoubleExtra("lat", 0.0)
                    val lng = it.getDoubleExtra("lng", 0.0)
                    if (lat != 0.0 && lng != 0.0) {
                        tripViewModel.updateJourneyLocation(lat, lng)
                    }
                }
            }
        }
        val filter = IntentFilter("LOCATION_UPDATE")
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Carica markers e journeys dal DB
    LaunchedEffect(currentTrip?.id) {
        currentTrip?.let {
            val fromDb = tripViewModel.getMarkersForTrip(it.id)
            markers.clear()
            markers.addAll(fromDb)
            tripViewModel.loadJourneysForTrip(it.id.toLong())
        }
    }



    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 2.dp,
                color = blu,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp)),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true
        ),
        onMapClick = { latLng ->
            selectedLatLng = latLng
            showMainDialog = true
        }
    ) {
        val journeyPoints by tripViewModel.journeyPoints.collectAsState()
        val alljourneyPoints by tripViewModel.allJourneyPoints.collectAsState()
        for (path in alljourneyPoints) {
            if (path.isNotEmpty()) {
                com.google.maps.android.compose.Polyline(
                    points = path,
                    color = ciano.copy(alpha = 0.7f),
                    width = 10f
                )
            } else {
                android.util.Log.d("TripMap", "Nessun punto nel percorso: Polyline non disegnata")
            }
        }

        if (journeyPoints.isNotEmpty()) {
            com.google.maps.android.compose.Polyline(
                points = journeyPoints,
                color = ciano,
                width = 10f
            )
        }

        markers.forEachIndexed { index, marker ->
            Marker(
                state = MarkerState(LatLng(marker.latitude, marker.longitude)),
                title = "Ricordo #${index + 1}",
                snippet = marker.note ?: "",
                onClick = {
                    selectedMarker = marker
                    showMarkerDialog = true
                    true
                }
            )
        }
    }

    if (showMarkerDialog && selectedMarker != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showMarkerDialog = false
                selectedMarker = null
            },
            title = { Text("Dettagli ricordo", style = myTipography2.titleLarge) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val note = selectedMarker?.note
                    val photoPath = selectedMarker?.photoPath

                    if (note.isNullOrEmpty() && photoPath.isNullOrEmpty()) {
                        Text(
                            text = "Non ci sono note e/o foto associati a questo ricordo.",
                            style = myTipography2.bodyLarge,
                            color = ciano
                        )
                    } else {
                        note?.takeIf { it.isNotEmpty() }?.let {
                            Text(it, style = myTipography2.bodyLarge)
                        }

                        photoPath?.let { path ->
                            val bitmap = try {
                                if (path.startsWith("content://")) {
                                    val stream = context.contentResolver.openInputStream(android.net.Uri.parse(path))
                                    android.graphics.BitmapFactory.decodeStream(stream).also { stream?.close() }
                                } else {
                                    android.graphics.BitmapFactory.decodeFile(path)
                                }
                            } catch (e: Exception) { null }

                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .border(
                                            width = 2.dp,
                                            color = ciano,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    coroutineScope.launch {
                        selectedMarker?.let { m ->
                            tripViewModel.deleteMarker(m)
                            markers.remove(m)
                        }
                        selectedMarker = null
                        showMarkerDialog = false
                    }
                }) { Text("Rimuovi marker", style = myTipography2.bodyLarge, color=ciano) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showMarkerDialog = false
                    selectedMarker = null
                }) { Text("Chiudi", style = myTipography2.bodyLarge, color=ciano) }
            }
        )
    }

    // Dialog principale di scelta
    if (showMainDialog && selectedLatLng != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMainDialog = false },
            title = { Text("Aggiungi un ricordo", style = myTipography2.titleLarge) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentPhotoPath !=null || currentNote.isNotEmpty())
                        Text("Clicca di nuovo sui bottoni per cambiare", style= myTipography2.labelMedium)
                    if (currentNote.isNotEmpty()) Text("Hai inserito una nota", style = myTipography2.bodyLarge)
                    if (currentPhotoPath != null) Text("Hai inserito una foto", style = myTipography2.bodyLarge)

                }
            },
            confirmButton = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        androidx.compose.material3.TextButton(onClick = {
                            showNoteDialog = true
                            showMainDialog = false
                        }) {
                            Column (
                                modifier = Modifier
                                    .padding(16.dp)
                                    .border(
                                        width = 3.dp,
                                        color = ciano,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ){
                                Text("Nota", style = myTipography2.bodyLarge, color=ciano)
                                Spacer(modifier = Modifier.height(2.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "note",
                                    tint=ciano
                                )
                            }

                        }

                        androidx.compose.material3.TextButton(onClick = {
                            showMainDialog = false
                            pickImageLauncher.launch("image/*")
                        }) {
                            Column (
                                modifier = Modifier
                                .padding(16.dp)
                                .border(
                                    width = 3.dp,
                                    color = ciano,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ){
                                Text("Foto", style = myTipography2.bodyLarge,color = ciano)
                                Spacer(modifier = Modifier.height(2.dp))
                                Icon(
                                    imageVector = Icons.Default.AccountBox,
                                    contentDescription = "photo",
                                    tint=ciano
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        androidx.compose.material3.TextButton(onClick = {
                            //Copia dei valori
                            val noteToSave = if (currentNote.isNotEmpty()) currentNote else null
                            val photoToSave = currentPhotoPath
                            val latLngToSave = selectedLatLng

                            currentTrip?.let { trip ->
                                latLngToSave?.let { latLng ->
                                    coroutineScope.launch {
                                        tripViewModel.addMarker(
                                            tripId = trip.id,
                                            lat = latLng.latitude,
                                            lng = latLng.longitude,
                                            note = noteToSave,
                                            photoPath = photoToSave
                                        )

                                        val updatedMarkers =
                                            tripViewModel.getMarkersForTrip(trip.id)
                                        markers.clear()
                                        markers.addAll(updatedMarkers)
                                    }
                                }
                            }

                            // reset stati
                            currentNote = ""
                            currentPhotoPath = null
                            selectedLatLng = null
                            showMainDialog = false
                        }) { Text("Fine", style = myTipography2.bodyLarge, color=ciano) }

                        androidx.compose.material3.TextButton(
                            onClick = {
                                showMainDialog = false
                                currentNote = ""
                                currentPhotoPath = null
                                selectedLatLng = null
                            }) { Text("Annulla", style = myTipography2.bodyLarge, color=ciano) }
                    }
                }
            },
            dismissButton = {
            }
        )
    }

    // Dialog per scrivere la nota
    if (showNoteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Scrivi una nota", style = myTipography2.labelMedium) },
            text = {
                androidx.compose.material3.TextField(
                    value = currentNote,
                    onValueChange = { currentNote = it },
                    placeholder = { Text("Scrivi qui...", style = myTipography2.bodyLarge) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = myTipography2.bodyLarge
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showNoteDialog = false
                    showMainDialog = true
                }) { Text("Salva", style = myTipography2.labelMedium, color=ciano) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showNoteDialog = false
                    showMainDialog = true
                }) { Text("Annulla", style = myTipography2.bodyLarge, color=ciano) }
            }
        )
    }
}

