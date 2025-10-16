package com.example.mytravelcompanion.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.google.gson.Gson

class TripViewModel(private val dao: TripDao,
                    private val markerDao: MarkerDAO,
                    private val journeyDao: JourneyDAO) : ViewModel() {

    var lastKnownLocation: LatLng? = null
    val trips = dao.getAllTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var _currentJourney = MutableStateFlow<Journey?>(null)
    val currentJourney: StateFlow<Journey?> get() = _currentJourney
    private var _journeyPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val journeyPoints: StateFlow<List<LatLng>> get() = _journeyPoints

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


    fun startJourney(tripId: Long) = viewModelScope.launch {
        val journey = Journey(tripId = tripId, start = System.currentTimeMillis())
        val id = journeyDao.insertJourney(journey)
        _currentJourney.value = journey.copy(id = id)
        _journeyPoints.value = emptyList()
    }

    fun updateJourneyLocation(lat: Double, lng: Double) = viewModelScope.launch {
        _currentJourney.value?.let { journey ->
            val points = _journeyPoints.value.toMutableList()
            points.add(LatLng(lat, lng))
            _journeyPoints.value = points

            // Salviamo il percorso nel DB come JSON
            val json = Gson().toJson(points.map { LatLngSerializable(it.latitude, it.longitude) })
            journeyDao.updatePath(journey.id, json)
        }
    }

    fun stopJourney() = viewModelScope.launch {
        _currentJourney.value?.let { journey ->
            journeyDao.updateEndTime(journey.id, System.currentTimeMillis())
            _currentJourney.value = null
            _journeyPoints.value = emptyList()
        }
    }
}
data class LatLngSerializable(val lat: Double, val lng: Double)