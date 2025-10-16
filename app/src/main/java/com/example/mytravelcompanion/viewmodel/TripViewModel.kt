package com.example.mytravelcompanion.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TripViewModel(private val dao: TripDao,
                    private val markerDao: MarkerDAO) : ViewModel() {

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

    suspend fun addMarker(tripId: Int, lat: Double, lng: Double, note: String?, photoPath: String?) {
        val marker = Marker(tripId = tripId, latitude = lat, longitude = lng, note = note, photoPath = photoPath)
        android.util.Log.d("TripVM", "SALVO marker -> note=$note, photo=$photoPath")
        markerDao.insertMarker(marker)
    }

    suspend fun getMarkersForTrip(tripId: Int): List<Marker> {
        val list = markerDao.getMarkersByTrip(tripId)
        android.util.Log.d("TripVM", "LETTI marker dal DB: ${list.size}")
        list.forEach {
            android.util.Log.d("TripVM", " -> id=${it.id}, note=${it.note}, photo=${it.photoPath}")
        }
        return list
    }

    suspend fun deleteMarkersForTrip(tripId: Int) {
        markerDao.deleteMarkersByTrip(tripId)
    }
    suspend fun deleteMarker(marker: Marker) {
        markerDao.deleteMarker(marker)
    }
}
