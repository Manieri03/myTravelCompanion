package com.example.mytravelcompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.data.Trip
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.data.TripViewModelFactory
import com.example.mytravelcompanion.ui.theme.ciano
import com.example.mytravelcompanion.ui.theme.myTipography2
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
    val pastTrips = trips
        .filter { it.endDate != null && it.endDate!!.isBefore(LocalDate.now()) }
        .sortedByDescending { it.endDate }
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
