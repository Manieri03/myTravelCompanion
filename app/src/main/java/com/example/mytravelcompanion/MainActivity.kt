package com.example.mytravelcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytravelcompanion.data.AppDatabase
import com.example.mytravelcompanion.data.TripViewModel
import com.example.mytravelcompanion.data.TripViewModelFactory
import com.example.mytravelcompanion.ui.theme.MyTravelCompanionTheme
import com.example.mytravelcompanion.navigation.NavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
           MyTravelCompanionTheme {
               val context = LocalContext.current
               val dao = AppDatabase.getDatabase(context).tripDao()
               val markerDAO = AppDatabase.getDatabase(context).MarkerDAO()
               val journeyDAO = AppDatabase.getDatabase(context).JourneyDAO()
               val tripViewModel: TripViewModel = viewModel(
                   factory = TripViewModelFactory(dao, markerDAO, journeyDAO)
               )
               LaunchedEffect(Unit) {
                   tripViewModel.checkAndUpdateTripCompletion()
               }
                NavHost()
            }
        }
    }
}
