package com.example.mytravelcompanion.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TripViewModelFactory(private val dao: TripDao, private  val markerDAO: MarkerDAO, private val journeyDAO: JourneyDAO, private val pointDAO: PointDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(dao,markerDAO, journeyDAO, pointDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
