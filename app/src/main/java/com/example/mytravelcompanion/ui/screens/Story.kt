package com.example.mytravelcompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytravelcompanion.R
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.data.Trip
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.data.TripViewModelFactory
import com.example.mytravelcompanion.ui.theme.ciano
import com.example.mytravelcompanion.ui.theme.myTipography2
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun Story() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).tripDao()
    val markerDAO = AppDatabase.getDatabase(context).MarkerDAO()
    val journeyDAO = AppDatabase.getDatabase(context).JourneyDAO()
    val tripViewModel: TripViewModel = viewModel(
        factory = TripViewModelFactory(dao, markerDAO, journeyDAO)
    )

    val trips by tripViewModel.trips.collectAsState(initial = emptyList())
    val pastTrips = trips.filter {
        (it.endDate != null && it.endDate!!.isBefore(LocalDate.now())) || it.isCompleted
    }.sortedByDescending { it.endDate ?: LocalDate.now() }
    val scrollState = rememberScrollState()

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
            pastTrips.forEach { trip ->
                TripCard(trip)
            }
        }
    }
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

        Text("Tipo: ${trip.tripType}", style = myTipography2.bodyLarge)
    }
}

@Composable
fun TripMapPreview(
    tripViewModel: TripViewModel,
    tripId: Long
) {
    val context = LocalContext.current
    val markers = remember { mutableStateListOf<com.example.mytravelcompanion.data.Marker>() }
    val journeyPoints by tripViewModel.allJourneyPoints.collectAsState()

    var initialCamera by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(tripId) {
        val tripMarkers = tripViewModel.getMarkersForTrip(tripId.toInt())
        markers.clear()
        markers.addAll(tripMarkers)

        tripViewModel.loadJourneysForTrip(tripId)

        val allJourneys = journeyPoints.flatten()
        if (allJourneys.isNotEmpty()) {
            initialCamera = allJourneys.first()
        } else if (tripMarkers.isNotEmpty()) {
            initialCamera = LatLng(tripMarkers.first().latitude, tripMarkers.first().longitude)
        }
    }

    if (initialCamera == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Caricamento mappa...", style = myTipography2.bodyLarge)
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCamera!!, 13f)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .border(2.dp, ciano, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    ) {

        for (path in journeyPoints) {
            if (path.isNotEmpty()) {
                com.google.maps.android.compose.Polyline(
                    points = path,
                    color = ciano.copy(alpha = 0.7f),
                    width = 8f
                )
            }
        }

        markers.forEachIndexed { index, marker ->
            Marker(
                state = MarkerState(LatLng(marker.latitude, marker.longitude)),
                title = "Ricordo #${index + 1}",
                snippet = marker.note ?: ""
            )
        }
    }
}

