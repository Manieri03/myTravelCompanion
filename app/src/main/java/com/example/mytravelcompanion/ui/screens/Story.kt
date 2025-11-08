package com.example.mytravelcompanion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mytravelcompanion.R
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.data.Trip
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.data.TripViewModelFactory
import com.example.mytravelcompanion.ui.theme.ciano
import com.example.mytravelcompanion.ui.theme.myTipography2
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun Story(tripViewModel: TripViewModel) {

    val trips by tripViewModel.trips.collectAsState(initial = emptyList())
    val pastTrips = trips.filter {
        (it.endDate != null && it.endDate!!.isBefore(LocalDate.now())) || it.isCompleted
    }.sortedByDescending { it.endDate ?: LocalDate.now() }

    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (selectedTrip == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, end = 18.dp, top = 55.dp, bottom = 18.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(45.dp)
        ) {
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
                    "I tuoi viaggi",
                    fontSize = 26.sp,
                    style = myTipography2.titleLarge
                )
            }

            var searchDestination by remember { mutableStateOf("") }
            var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
            var showStartModal by remember { mutableStateOf(false) }
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            var filtersExpanded by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { filtersExpanded = !filtersExpanded }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (filtersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (filtersExpanded) "Nascondi filtri" else "Mostra filtri",
                        tint = ciano,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Filtri",
                        style = myTipography2.labelMedium,
                        fontSize = 20.sp,
                        color = ciano
                    )
                }

                //Filtri
                AnimatedVisibility(visible = filtersExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        //Ricerca destinazione
                        OutlinedTextField(
                            value = searchDestination,
                            onValueChange = { searchDestination = it },
                            placeholder = { Text("Cerca destinazione...", style = myTipography2.bodyLarge) },
                            textStyle = myTipography2.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Filtro data
                        OutlinedTextField(
                            value = selectedStartDate?.format(formatter) ?: "",
                            onValueChange = {},
                            label = { Text("Data partenza") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showStartModal = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Seleziona data")
                                }
                            }
                        )
                        if (showStartModal) {
                            DatePickerModal(
                                onDateSelected = { selectedStartDate = it },
                                onDismiss = { showStartModal = false }
                            )
                        }

                        // Reset filtri
                        Button(
                            onClick = {
                                searchDestination = ""
                                selectedStartDate = null
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Filtri resettati correttamente")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ciano),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                        ) {
                            Text("Reset filtri", style = myTipography2.bodyLarge)
                        }
                    }
                }
            }

            if (pastTrips.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Nessun viaggio completato",
                        style = myTipography2.bodyLarge
                    )
                }
            } else {

                val filteredTrips = pastTrips.filter { trip ->
                    (searchDestination.isBlank() || trip.destination.contains(searchDestination, ignoreCase = true)) &&
                            (selectedStartDate == null || trip.startDate == selectedStartDate)
                }

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    filteredTrips.forEach { trip ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTrip = trip }
                        ) {
                            TripCard(trip)
                        }
                    }
                }
            }
        }
    }else{
        var totalDistanceKm by remember { mutableStateOf<Double?>(null) }

        LaunchedEffect(selectedTrip) {
            selectedTrip?.let {
                totalDistanceKm = tripViewModel.getTotalDistanceForTrip(it.id)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mappa del viaggio a ${selectedTrip!!.destination}",
                style = myTipography2.titleLarge
            )

            TripMapPreview(
                tripViewModel = tripViewModel,
                tripId = selectedTrip!!.id
            )

            Row(
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
            ) {

                totalDistanceKm?.let {
                    Text(
                        text = "Distanza totale percorsa: ${"%.2f".format(it)} km",
                        style = myTipography2.titleMedium,
                        color = ciano
                    )
                }
            }

            Row(
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
            ){
                Button(
                    onClick = { selectedTrip = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ciano)) {
                    Text("Indietro", style = myTipography2.bodyLarge)
                }

                Button(
                    onClick = {
                        tripViewModel.deleteTrip(selectedTrip!!)
                        selectedTrip = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Elimina viaggio", style = myTipography2.bodyLarge, color = Color.White)
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

@Composable
fun TripCard(trip: Trip) {

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(12.dp)
            .border(width = 2.dp, color = ciano, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(trip.destination, style = myTipography2.titleMedium, fontSize = 20.sp)

        trip.startDate?.let { start ->
            trip.endDate?.let { end ->
                val formattedStart = start.format(formatter)
                val formattedEnd = end.format(formatter)
                Text("Dal $formattedStart al $formattedEnd", style = myTipography2.bodyLarge)
            }
        }

        Text("Tipo: ${trip.tripType.displayName}", style = myTipography2.bodyLarge)
    }
}

@Composable
fun TripMapPreview(
    tripViewModel: TripViewModel,
    tripId: Int
) {
    val context = LocalContext.current
    val markers = remember { mutableStateListOf<com.example.mytravelcompanion.data.Marker>() }
    val journeyPoints by tripViewModel.allJourneyPoints.collectAsState()
    val journeys by tripViewModel.journeys.collectAsState()

    var initialCamera by remember { mutableStateOf<LatLng?>(null) }
    var selectedMarker by remember { mutableStateOf<com.example.mytravelcompanion.data.Marker?>(null) }
    var showMarkerDialog by remember { mutableStateOf(false) }

    var selectedJourney by remember { mutableStateOf<com.example.mytravelcompanion.data.Journey?>(null) }
    var showJourneyDialog by remember { mutableStateOf(false) }

    var customNoteIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var customPhotoIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var customBothIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var customPointIcon by remember {mutableStateOf<BitmapDescriptor?>(null)}

    val points by tripViewModel.points.collectAsState(initial = emptyList())

    // Caricamento iniziale dati
    LaunchedEffect(tripId) {
        val tripMarkers = tripViewModel.getMarkersForTrip(tripId)
        markers.clear()
        markers.addAll(tripMarkers)
        tripViewModel.loadJourneysForTrip(tripId)
        tripViewModel.loadPoints(context)

        val paths = tripViewModel.allJourneyPoints.first()
        initialCamera = paths.flatten().firstOrNull()
            ?: tripMarkers.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
                    ?: LatLng(44.4949, 11.3426)
    }

    val defaultLocation = LatLng(44.4949, 11.3426)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 13f)
    }

    LaunchedEffect(initialCamera) {
        initialCamera?.let { cam ->
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(cam, 13f))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .border(2.dp, ciano, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = true),
            onMapLoaded = {
                try {
                    val sizeDp = 40
                    val density = context.resources.displayMetrics.density
                    val sizePx = (sizeDp * density).toInt()

                    fun loadScaledIcon(resId: Int): BitmapDescriptor {
                        val bmp =
                            android.graphics.BitmapFactory.decodeResource(context.resources, resId)
                        val scaled =
                            android.graphics.Bitmap.createScaledBitmap(bmp, sizePx, sizePx, false)
                        return BitmapDescriptorFactory.fromBitmap(scaled)
                    }

                    customNoteIcon = loadScaledIcon(R.drawable.pin_note)
                    customPhotoIcon = loadScaledIcon(R.drawable.pin_photo)
                    customBothIcon = loadScaledIcon(R.drawable.pin_note_photo)
                    customPointIcon=loadScaledIcon(R.drawable.point_interest_icon)
                } catch (e: Exception) {
                    val fallback =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                    customNoteIcon = fallback
                    customPhotoIcon = fallback
                    customBothIcon = fallback
                    customPointIcon=fallback
                }
            }
        ) {
            //journeys
            journeyPoints.forEachIndexed { index, path ->
                if (path.isNotEmpty()) {
                    com.google.maps.android.compose.Polyline(
                        points = path,
                        color = ciano.copy(alpha = 0.7f),
                        width = 25f,
                        clickable = true,
                        onClick = {
                            selectedJourney = journeys.getOrNull(index)
                            showJourneyDialog = true
                        }
                    )
                }
            }

            //markers
            markers.forEachIndexed { index, marker ->
                val hasNote = !marker.note.isNullOrEmpty()
                val hasPhoto = !marker.photoPath.isNullOrEmpty()

                val customIcon = when {
                    hasNote && hasPhoto -> customBothIcon
                    hasNote -> customNoteIcon
                    hasPhoto -> customPhotoIcon
                    else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                }

                Marker(
                    state = MarkerState(LatLng(marker.latitude, marker.longitude)),
                    title = "Ricordo #${index + 1}",
                    snippet = marker.note ?: "",
                    icon = customIcon,
                    onClick = {
                        selectedMarker = marker
                        showMarkerDialog = true
                        true
                    }
                )
            }

            points.forEach { point ->
                Marker(
                    state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                    title = point.name,
                    icon = customPointIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                )
            }
        }

        // dialogs

        if (showJourneyDialog && selectedJourney != null) {
            AlertDialog(
                onDismissRequest = {
                    showJourneyDialog = false
                    selectedJourney = null
                },
                title = { Text("Dettagli percorso", style = myTipography2.titleLarge) },
                text = {
                    val km = selectedJourney!!.distanceMeters?.div(1000)
                    val duration = selectedJourney!!.durationSeconds
                    val minutes = duration?.div(60)
                    val seconds = duration?.rem(60)
                    Column {
                        Text("Distanza: ${"%.2f".format(km)} km", style = myTipography2.bodyLarge)
                        Text("Durata: ${minutes} min ${seconds} sec", style = myTipography2.bodyLarge)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showJourneyDialog = false
                        selectedJourney = null
                    }) { Text("Chiudi", color = ciano) }
                }
            )
        }

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
                                "Non ci sono note o foto associate a questo ricordo.",
                                style = myTipography2.bodyLarge,
                                color = ciano
                            )
                        } else {
                            note?.takeIf { it.isNotEmpty() }?.let {
                                Text(it, style = myTipography2.bodyLarge)
                            }

                            photoPath?.let { path ->
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(context)
                                        .data(path)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto ricordo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(2.dp, ciano, RoundedCornerShape(16.dp))
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showMarkerDialog = false
                            selectedMarker = null
                        }
                    ) { Text("Chiudi", style = myTipography2.bodyLarge, color = ciano) }
                }
            )
        }
    }
}

