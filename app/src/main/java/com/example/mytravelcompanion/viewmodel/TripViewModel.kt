package com.example.mytravelcompanion.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TripViewModel(private val dao: TripDao) : ViewModel() {

    var lastKnownLocation: LatLng? = null
    val trips = dao.getAllTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            dao.insertTrip(trip)
        }
    }

    fun deleteAllTrips() {
        viewModelScope.launch {
            dao.deleteAllTrips()
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            dao.deleteTrip(trip)
        }
    }

    fun getCurrentTrip(trips: List<Trip>): Trip? {
        val today = LocalDate.now()
        return trips.find { it.startDate != null && it.endDate != null &&
                today >= it.startDate && today <= it.endDate }
    }
}
