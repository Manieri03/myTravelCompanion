package com.example.mytravelcompanion.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.mytravelcompanion.R
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.ui.theme.myTipography2
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.mytravelcompanion.data.Trip
import com.example.mytravelcompanion.service.JourneyService
import com.example.mytravelcompanion.ui.theme.blu
import com.example.mytravelcompanion.ui.theme.ciano
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mytravelcompanion.util.PhotoHelper.compressImage

@Composable
fun Live(navController: NavController, tripViewModel: TripViewModel) {
    val context = LocalContext.current

    val trips by tripViewModel.trips.collectAsState(initial = emptyList())
    val currentTrip = tripViewModel.getCurrentTrip(trips)

    val isJourneyActive by tripViewModel.isJourneyActive.collectAsState()
    val serviceIntent = Intent(context, JourneyService::class.java)

    val distance by tripViewModel.liveDistanceMeters.collectAsState()
    val duration by tripViewModel.liveDurationSeconds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(RequestPermission()) { isGranted: Boolean -> }

    // Se non concesso l'autorizzazione a fine location, la chiediamo
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 18.dp, end = 18.dp, top = 55.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currentTrip.destination,
                    fontSize = 22.sp,
                    style = myTipography2.bodyLarge,
                    color = ciano
                )
            }
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
                    Row (
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ){
                        Button(
                            onClick = {
                                if (!isJourneyActive) {
                                    if (hasLocationPermission) {
                                        // Avvio percorso e service
                                        currentTrip.id
                                            .let { tripViewModel.startJourney(it) }
                                        serviceIntent.putExtra("tripId", currentTrip.id)
                                        ContextCompat.startForegroundService(context, serviceIntent)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Registrazione del percorso avviata")
                                        }
                                    } else {
                                        // Richiedi permesso
                                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                } else {
                                    // Ferma percorso e poi service
                                    tripViewModel.stopJourney()
                                    context.stopService(serviceIntent)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Registrazione del percorso stoppata")
                                    }
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = ciano)
                        ) {
                            Text(
                                if (!isJourneyActive) "Avvia" else "Stop",
                                style = myTipography2.bodyLarge
                            )
                        }

                        Button(
                            onClick = {
                                currentTrip?.let {
                                    tripViewModel.markTripAsCompleted(it.id,context)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Hai concluso il tuo viaggio")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = blu)
                        ) {
                            Text("Fine viaggio", style = myTipography2.bodyLarge)
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ){

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Distanza: ${"%.2f".format(distance / 1000)} km")
                            Text("Durata: ${duration / 60} min ${duration % 60} sec")
                        }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Text(
                    text = "Nessun viaggio previsto per oggi",
                    style = myTipography2.bodyLarge
                )

                Button(
                    onClick = { navController.navigate("Plan") },
                    colors = ButtonDefaults.buttonColors(containerColor = ciano),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Icona pianifica",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pianificalo",
                        style = MaterialTheme.typography.bodyLarge
                    )

                }

            }
        }

    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        snackbar = { snackbarData ->
            Snackbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = ciano,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    color = Color.White,
                    style=myTipography2.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
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

    var customNoteIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var customPhotoIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var customBothIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var customPointIcon by remember {mutableStateOf<BitmapDescriptor?>(null)}

    val points by tripViewModel.points.collectAsState(initial = emptyList())

    var showPhotoChoiceDialog by remember { mutableStateOf(false) }

    // Recupera la posizione attuale al primo avvio
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                startLatLng = LatLng(it.latitude, it.longitude)
                tripViewModel.lastKnownLocation =
                    startLatLng // opzionale, se vuoi mantenerla nel ViewModel
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
            currentPhotoPath = compressImage(context, file.absolutePath)
        }
        showMainDialog = true
    }

    var photoFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoFile?.absolutePath?.let { originalPath ->
                currentPhotoPath = compressImage(context, originalPath)
            }
            showMainDialog = true
        }

    }

    // Aggiornamento posizione utente
    LaunchedEffect(isJourneyActive) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val newLatLng = LatLng(loc.latitude, loc.longitude)
                userLocation = newLatLng
                tripViewModel.lastKnownLocation = newLatLng

                android.util.Log.d(
                    "TripMap",
                    "Nuova posizione ricevuta: ${loc.latitude}, ${loc.longitude}"
                )

                if (isJourneyActive) {
                    android.util.Log.d(
                        "TripMap",
                        "Invio posizione al ViewModel per il percorso attivo"
                    )
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
            tripViewModel.loadJourneysForTrip(it.id)
        }
    }

    val alljourneyPoints by tripViewModel.allJourneyPoints.collectAsState()
    val journeys by tripViewModel.journeys.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(width = 2.dp, color = blu, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {

        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true
            ),
            onMapLoaded = {
                try {
                    val sizeDp = 40
                    val density = context.resources.displayMetrics.density
                    val sizePx = (sizeDp * density).toInt()

                    fun loadScaledIcon(resId: Int): BitmapDescriptor {
                        val bmp = android.graphics.BitmapFactory.decodeResource(context.resources, resId)
                        val scaled = android.graphics.Bitmap.createScaledBitmap(bmp, sizePx, sizePx, false)
                        return BitmapDescriptorFactory.fromBitmap(scaled)
                    }

                    customNoteIcon = loadScaledIcon(R.drawable.pin_note)
                    customPhotoIcon = loadScaledIcon(R.drawable.pin_photo)
                    customBothIcon = loadScaledIcon(R.drawable.pin_note_photo)
                    customPointIcon=loadScaledIcon(R.drawable.point_interest_icon)

                    android.util.Log.d("TripMap", "Icone personalizzate caricate correttamente")
                } catch (e: Exception) {
                    android.util.Log.e("TripMap", "Errore caricamento icone marker", e)
                    val fallback =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                    customNoteIcon = fallback
                    customPhotoIcon = fallback
                    customBothIcon = fallback
                    customPointIcon=fallback
                }
            },
            onMapClick = { latLng ->
                selectedLatLng = latLng
                showMainDialog = true
            },
        ) {
            // polilinee (viaggi)
            alljourneyPoints.forEachIndexed { _, path ->
                if (path.isNotEmpty()) {
                    com.google.maps.android.compose.Polyline(
                        points = path,
                        color = ciano.copy(alpha = 0.7f),
                        width = 15f,
                    )
                }
            }

            if (journeyPoints.isNotEmpty()) {
                com.google.maps.android.compose.Polyline(
                    points = journeyPoints,
                    color = ciano,
                    width = 15f
                )
            }

            // Markers
            markers.forEachIndexed { index, marker ->
                val hasNote = !marker.note.isNullOrEmpty()
                val hasPhoto = !marker.photoPath.isNullOrEmpty()

                val iconToUse = when {
                    hasNote && hasPhoto -> customBothIcon
                    hasNote -> customNoteIcon
                    hasPhoto -> customPhotoIcon
                    else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                }

                Marker(
                    state = MarkerState(LatLng(marker.latitude, marker.longitude)),
                    title = "Ricordo #${index + 1}",
                    snippet = marker.note ?: "",
                    icon = iconToUse,
                    onClick = {
                        selectedMarker = marker
                        showMarkerDialog = true
                        true
                    }
                )
            }

            points.forEach { point ->
                val position = LatLng(point.latitude, point.longitude)
                Marker(
                    state = MarkerState(position = position),
                    title = point.name,
                    icon = customPointIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                )
            }
        }

        //dialogs

        if (showMarkerDialog && selectedMarker != null) {
            AlertDialog(
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
                                "Non ci sono note e/o foto per questo ricordo.",
                                style = myTipography2.bodyLarge,
                                color = ciano
                            )
                        } else {
                            note?.takeIf { it.isNotEmpty() }?.let {
                                Text(it, style = myTipography2.bodyLarge)
                            }

                            photoPath?.let { path ->
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(path)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto ricordo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .border(width = 2.dp, color = ciano, shape = RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }

                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            selectedMarker?.let { m ->
                                tripViewModel.deleteMarker(m)
                                markers.remove(m)
                            }
                            selectedMarker = null
                            showMarkerDialog = false
                        }
                    }) { Text("Rimuovi marker", style = myTipography2.bodyLarge, color = ciano) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showMarkerDialog = false
                        selectedMarker = null
                    }) { Text("Chiudi", style = myTipography2.bodyLarge, color = ciano) }
                }
            )
        }

        if (showMainDialog && selectedLatLng != null) {
            AlertDialog(
                onDismissRequest = { showMainDialog = false },
                title = { Text("Aggiungi un ricordo", style = myTipography2.titleLarge) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentPhotoPath != null || currentNote.isNotEmpty())
                            Text(
                                "Clicca di nuovo sui bottoni per cambiare",
                                style = myTipography2.labelMedium
                            )
                        if (currentNote.isNotEmpty()) Text(
                            "Hai inserito una nota",
                            style = myTipography2.bodyLarge
                        )
                        if (currentPhotoPath != null) Text(
                            "Hai inserito una foto",
                            style = myTipography2.bodyLarge
                        )
                    }
                },
                confirmButton = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = {
                                showNoteDialog = true
                                showMainDialog = false
                            }) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .border(
                                            width = 3.dp,
                                            color = ciano,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Nota", style = myTipography2.bodyLarge, color = ciano)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "note",
                                        tint = ciano
                                    )
                                }
                            }

                            TextButton(onClick = {
                                showPhotoChoiceDialog = true
                            }) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .border(
                                            width = 3.dp,
                                            color = ciano,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Foto", style = myTipography2.bodyLarge, color = ciano)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Icon(
                                        Icons.Default.AccountBox,
                                        contentDescription = "photo",
                                        tint = ciano
                                    )
                                }
                            }

                            if (showPhotoChoiceDialog) {
                                AlertDialog(
                                    onDismissRequest = { showPhotoChoiceDialog = false },
                                    title = { Text("Aggiungi foto", style = myTipography2.titleLarge) },
                                    text = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(25.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Button(
                                                onClick = {
                                                    showPhotoChoiceDialog = false
                                                    pickImageLauncher.launch("image/*")
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ciano)
                                            ) {
                                                Text("Scegli dalla galleria", style = myTipography2.bodyLarge)
                                            }

                                            Button(
                                                onClick = {
                                                    showPhotoChoiceDialog = false
                                                    photoFile = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
                                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile!!)
                                                    takePictureLauncher.launch(uri)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = blu)
                                            ) {
                                                Text("Scatta una foto", style = myTipography2.bodyLarge)
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showPhotoChoiceDialog = false
                                            showMainDialog = true
                                        }) { Text("Annulla", color = ciano, style = myTipography2.bodyLarge) }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = {
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

                                currentNote = ""
                                currentPhotoPath = null
                                selectedLatLng = null
                                showMainDialog = false
                            }) { Text("Fine", style = myTipography2.bodyLarge, color = ciano) }

                            TextButton(onClick = {
                                showMainDialog = false
                                currentNote = ""
                                currentPhotoPath = null
                                selectedLatLng = null
                            }) { Text("Annulla", style = myTipography2.bodyLarge, color = ciano) }
                        }
                    }
                }
            )
        }

        if (showNoteDialog) {
            AlertDialog(
                onDismissRequest = { showNoteDialog = false },
                title = { Text("Scrivi una nota", style = myTipography2.labelMedium) },
                text = {
                    TextField(
                        value = currentNote,
                        onValueChange = { currentNote = it },
                        placeholder = { Text("Scrivi qui...", style = myTipography2.bodyLarge) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = myTipography2.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showNoteDialog = false
                        showMainDialog = true
                    }) { Text("Salva", style = myTipography2.labelMedium, color = ciano) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showNoteDialog = false
                        showMainDialog = true
                    }) { Text("Annulla", style = myTipography2.bodyLarge, color = ciano) }
                }
            )
        }
    }
}

