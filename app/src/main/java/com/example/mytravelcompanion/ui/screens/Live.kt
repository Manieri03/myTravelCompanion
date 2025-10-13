package com.example.mytravelcompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.data.TripViewModelFactory
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.ui.theme.myTipography2
import java.time.LocalDate

@Composable
fun Live() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).tripDao()
    val tripViewModel: TripViewModel = viewModel(
        factory = TripViewModelFactory(dao)
    )

    val trips by tripViewModel.trips.collectAsState(initial = emptyList())
    val currentTrip = tripViewModel.getCurrentTrip(trips)

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentTrip?.let { "Buon viaggio!" } ?: "Nessun viaggio",
                fontSize = 26.sp,
                style = myTipography2.titleLarge
            )
        }



    }
}
