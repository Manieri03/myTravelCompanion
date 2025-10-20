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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private var _isJourneyActive = MutableStateFlow(false)
    val isJourneyActive: StateFlow<Boolean> get() = _isJourneyActive

    private var _allJourneyPoints = MutableStateFlow<List<List<LatLng>>>(emptyList())
    val allJourneyPoints: StateFlow<List<List<LatLng>>> get() = _allJourneyPoints

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
        _currentJourney.value?.let { current ->
            // salva nel DB
            val json = Gson().toJson(_journeyPoints.value.map {
                LatLngSerializable(it.latitude, it.longitude)
            })
            journeyDao.updatePath(current.id, json)
            journeyDao.updateEndTime(current.id, System.currentTimeMillis())
            android.util.Log.d("TripVM", "Journey precedente salvato (id=${current.id})")

            // salva nella lista di tutti i journey per mostrarli sulla mappa
            _allJourneyPoints.value = _allJourneyPoints.value + listOf(_journeyPoints.value)
        }

        // nuovo journey
        val journey = Journey(tripId = tripId, start = System.currentTimeMillis())
        val id = journeyDao.insertJourney(journey)
        _currentJourney.value = journey.copy(id = id)
        _journeyPoints.value = emptyList() // nuovo journey inizia da zero
        _isJourneyActive.value = true
        android.util.Log.d("TripVM", "Nuovo journey avviato (id=$id, trip=$tripId)")
    }



    fun updateJourneyLocation(lat: Double, lng: Double) {
        val journey = _currentJourney.value
        if (journey == null) {
            android.util.Log.w("TripVM", "Journey è null")
            return
        }

        if (!_isJourneyActive.value) {
            android.util.Log.w("TripVM", "Journey non attivo")
            return
        }

        val newPoint = LatLng(lat, lng)
        _journeyPoints.value = _journeyPoints.value + newPoint

        android.util.Log.d("TripVM", "Punto aggiunto: $lat, $lng - Totale punti attuali: ${_journeyPoints.value.size}")

        viewModelScope.launch {
            try {
                val json = Gson().toJson(_journeyPoints.value.map {
                    LatLngSerializable(it.latitude, it.longitude)
                })
                journeyDao.updatePath(journey.id, json)
                android.util.Log.d("TripVM", "Path salvato nel DB per journey ${journey.id}")
            } catch (e: Exception) {
                android.util.Log.e("TripVM", "Errore salvando path: ${e.message}")
            }
        }
    }


    fun stopJourney() = viewModelScope.launch {
        android.util.Log.d("TripVM", "STOP JOURNEY")
        _currentJourney.value?.let { journey ->
            journeyDao.updateEndTime(journey.id, System.currentTimeMillis())
            _allJourneyPoints.value = _allJourneyPoints.value + listOf(_journeyPoints.value)
            _currentJourney.value = null
            _isJourneyActive.value = false
            android.util.Log.d("TripVM", "Journey ${journey.id} terminato. Punti totali: ${_journeyPoints.value.size}")
        }
    }

    suspend fun loadJourneysForTrip(tripId: Long) {
        val journeys = journeyDao.getJourneysForTrip(tripId)
        val gson = Gson()
        val allPaths = mutableListOf<List<LatLng>>()

        for (j in journeys) {
            if (!j.path.isNullOrEmpty()) {
                try {
                    val list = gson.fromJson(
                        j.path,
                        Array<LatLngSerializable>::class.java
                    ).map { LatLng(it.lat, it.lng) }
                    allPaths.add(list)
                } catch (e: Exception) {
                    android.util.Log.e("TripVM", "Errore parsing percorso ${j.id}: ${e.message}")
                }
            }
        }

        _allJourneyPoints.value = allPaths
    }

    fun printAllJourneys() {
        viewModelScope.launch {
            val journeys = withContext(Dispatchers.IO) {
                journeyDao.getAllJourneys()
            }
            journeys.forEach { journey ->
                println(journey)
            }
        }
    }

    fun deleteAllJourneys() {
        viewModelScope.launch {
            journeyDao.deleteAllJourneys()
            _allJourneyPoints.value = emptyList()
            _currentJourney.value = null
            _journeyPoints.value = emptyList()
            _isJourneyActive.value = false
            android.util.Log.d("TripVM", "Tutti i journey eliminati dal DB")
        }
    }

    fun deleteJourneysForTrip(tripId: Long) {
        viewModelScope.launch {
            journeyDao.deleteJourneysForTrip(tripId)
            _allJourneyPoints.value = _allJourneyPoints.value.filterNot { false }
            android.util.Log.d("TripVM", "Journey del viaggio $tripId eliminati")
        }
    }


}


data class LatLngSerializable(val lat: Double, val lng: Double)