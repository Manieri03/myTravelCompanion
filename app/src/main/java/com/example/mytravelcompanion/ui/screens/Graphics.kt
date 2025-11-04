package com.example.mytravelcompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.mytravelcompanion.R
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.data.Trip
import com.example.mytravelcompanion.ui.theme.ciano
import com.example.mytravelcompanion.ui.theme.myTipography2
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.gson.Gson
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.heatmaps.HeatmapTileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun Graphics() {
    val context = LocalContext.current
    val tripDao = AppDatabase.getDatabase(context).tripDao()
    var monthlyTripCount by remember { mutableStateOf(List(12) { 0 }) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val trips = tripDao.getAllTripsOnce()
            val counts = MutableList(12) { 0 }

            val currentYear = Year.now().value
            trips.forEach { trip ->
                trip.startDate?.let { date ->
                    if (date.year == currentYear) {
                        val monthIndex = date.monthValue - 1
                        counts[monthIndex] += 1
                    }
                }
            }

            monthlyTripCount = counts
            loading = false
        }
    }

    val verticalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 18.dp, end = 18.dp, top = 55.dp, bottom = 18.dp)
            .verticalScroll(verticalScrollState),
        verticalArrangement = Arrangement.spacedBy(40.dp)
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
                "I tuoi grafici",
                fontSize = 26.sp,
                style = myTipography2.titleLarge
            )
        }



        Column(
            modifier=Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Heat map dei tuoi percorsi", style=myTipography2.labelMedium, fontSize = 22.sp)
            JourneyHeatmapScreen()
            if (loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Caricamento grafico...", style = myTipography2.bodyLarge)
                }
            } else {
                Text("Numero di viaggi", style=myTipography2.labelMedium, fontSize = 22.sp)
                MonthlyTripsChart(monthlyTripCount)
            }
        }


    }
}

@Composable
fun JourneyHeatmapScreen() {
    val context = LocalContext.current
    val journeyDAO = AppDatabase.getDatabase(context).JourneyDAO()

    var heatmapPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showLastMonthOnly by remember { mutableStateOf(false) }
    val now = LocalDate.now()
    val firstDayOfMonth = now.withDayOfMonth(1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (showLastMonthOnly) "Mostra tutti" else "Mostra ultimo mese",
            modifier = Modifier
                .clickable { showLastMonthOnly = !showLastMonthOnly }
                .padding(8.dp)
                .border(1.dp, ciano, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }

    // Carica i percorsi di tutti i journey dal DB
    LaunchedEffect(showLastMonthOnly) {
        loading = true
        withContext(Dispatchers.IO) {
            val gson = Gson()
            val journeys = journeyDAO.getAllJourneys()
            val allPoints = mutableListOf<LatLng>()

            val now = LocalDate.now()
            val firstDayOfMonth = now.withDayOfMonth(1)
            journeys.forEach { journey ->
                if (!journey.path.isNullOrEmpty()) {
                    // converto il Long in LocalDate
                    val journeyDate = journey.start?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }

                    // filtro per ultimo mese
                    if (!showLastMonthOnly || (journeyDate != null && !journeyDate.isBefore(firstDayOfMonth))) {
                        try {
                            val points = gson.fromJson(
                                journey.path,
                                Array<com.example.mytravelcompanion.data.LatLngSerializable>::class.java
                            ).map { LatLng(it.lat, it.lng) }
                            allPoints.addAll(points)
                        } catch (e: Exception) {
                            android.util.Log.e("Heatmap", "Errore parsing percorso ${journey.id}: ${e.message}")
                        }
                    }
                }
            }

            heatmapPoints = allPoints
            loading = false
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Caricamento heatmap...", style = myTipography2.bodyLarge)
        }
        return
    }

    if (heatmapPoints.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nessun percorso registrato", style = myTipography2.bodyLarge)
        }
        return
    }

    val firstPoint = heatmapPoints.first()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(firstPoint, 6f)
    }

    // Mappa + Heatmap
    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(2.dp, ciano, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    ) {
        MapEffect(heatmapPoints) { map ->
            map.clear()
            val colors = intArrayOf(
                android.graphics.Color.rgb(0, 255, 255),
                android.graphics.Color.rgb(0, 0, 255),
                android.graphics.Color.rgb(255, 0, 0)
            )
            val startPoints = floatArrayOf(0.2f, 0.5f, 1.0f)

            val gradient = com.google.maps.android.heatmaps.Gradient(colors, startPoints)

            val provider = HeatmapTileProvider.Builder()
                .data(heatmapPoints)
                .radius(40)
                .gradient(gradient)
                .maxIntensity(30.0)
                .build()
            map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
        }
    }
}

@Composable
fun MonthlyTripsChart(monthlyTripCount: List<Int>) {
    val context=LocalContext.current
    val max = monthlyTripCount.maxOrNull()?.toFloat() ?: 1f
    val scrollState = rememberScrollState()
    val tripDao = AppDatabase.getDatabase(context).tripDao()

    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }
    var monthTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("it"))

    LaunchedEffect(selectedMonthIndex) {
        selectedMonthIndex?.let { monthIndex ->
            val year = Year.now().value
            val month = monthIndex + 1 // da 0–11 a 1–12

            val firstDay = LocalDate.of(year, month, 1)
            val lastDay = YearMonth.of(year, month).atEndOfMonth()

            monthTrips = withContext(Dispatchers.IO) {
                tripDao.getTripsForMonth(firstDay, lastDay)
            }
        }
    }

    // Dialog
    selectedMonthIndex?.let { monthIndex ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedMonthIndex = null },
            title = { Text("Viaggi di ${java.text.DateFormatSymbols().months[monthIndex]}", style=myTipography2.titleLarge) },
            text = {
                if (monthTrips.isEmpty()) {
                    Text("Nessun viaggio registrato", style=myTipography2.labelMedium)
                } else {
                    Column {
                        monthTrips.forEach { trip ->
                            val formattedDate = trip.startDate?.format(formatter) ?: ""
                            Text("- ${trip.destination} ($formattedDate)", style=myTipography2.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { selectedMonthIndex = null }) {
                    Text("Chiudi")
                }
            }
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        monthlyTripCount.forEachIndexed { index, count ->
            val heightRatio = (count / max)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.width(40.dp)
                    .clickable { selectedMonthIndex = index }
            ) {
                Box(
                    modifier = Modifier
                        .height(150.dp * heightRatio)
                        .width(20.dp)
                        .border(1.dp, ciano)
                        .background(ciano)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(java.text.DateFormatSymbols().shortMonths[index], fontSize = 10.sp)
                Text(count.toString(), fontSize = 10.sp)
            }
        }
    }
}
