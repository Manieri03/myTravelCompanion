package com.example.mytravelcompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun Graphics() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 18.dp, end = 18.dp, top = 55.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
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
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            JourneyHeatmapScreen()
        }


    }
}

@Composable
fun JourneyHeatmapScreen() {
    val context = LocalContext.current
    val journeyDAO = AppDatabase.getDatabase(context).JourneyDAO()

    var heatmapPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Carica i percorsi di tutti i journey dal DB
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val gson = Gson()
            val journeys = journeyDAO.getAllJourneys()
            val allPoints = mutableListOf<LatLng>()

            journeys.forEach { journey ->
                if (!journey.path.isNullOrEmpty()) {
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
            val provider = HeatmapTileProvider.Builder()
                .data(heatmapPoints)
                .radius(30)
                .build()
            map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
        }
    }
}