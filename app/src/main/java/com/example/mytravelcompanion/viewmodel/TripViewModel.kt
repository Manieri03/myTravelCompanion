package com.example.mytravelcompanion.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytravelcompanion.util.DistanceCalculator
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.forEach
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

    private val _photos = MutableStateFlow<Map<String, List<MarkerWithTripName>>>(emptyMap())
    val photos: StateFlow<Map<String, List<MarkerWithTripName>>> get() = _photos

    private val _liveDistanceMeters = MutableStateFlow(0.0)
    val liveDistanceMeters: StateFlow<Double> get() = _liveDistanceMeters

    private val _liveDurationSeconds = MutableStateFlow(0L)
    val liveDurationSeconds: StateFlow<Long> get() = _liveDurationSeconds

    private val _journeys = MutableStateFlow<List<Journey>>(emptyList())
    val journeys: StateFlow<List<Journey>> get() = _journeys

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
        return trips.find {
            it.startDate != null && it.endDate != null &&
                    today >= it.startDate && today <= it.endDate &&
                    !it.isCompleted
        }
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


    fun startJourney(tripId: Int) = viewModelScope.launch {
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
        val journey = _currentJourney.value ?: return
        if (!_isJourneyActive.value) return

        val newPoint = LatLng(lat, lng)
        val lastPoint = _journeyPoints.value.lastOrNull()

        if (lastPoint != null) {
            val delta = DistanceCalculator.distanceBetween(
                LatLngSerializable(lastPoint.latitude, lastPoint.longitude),
                LatLngSerializable(newPoint.latitude, newPoint.longitude)
            )

            // Ignoro salti improvvisi (per debug posizione simulata)
            if (delta > 1000) {
                android.util.Log.w("TripVM", "Salto anomalo di ${"%.2f".format(delta)} m — punto ignorato")
                return
            }
        }

        _journeyPoints.value = _journeyPoints.value + newPoint

        _liveDistanceMeters.value = DistanceCalculator.totalDistance(
            _journeyPoints.value.map { LatLngSerializable(it.latitude, it.longitude) }
        )
        _liveDurationSeconds.value = ((System.currentTimeMillis() - journey.start) / 1000)

        viewModelScope.launch {
            val json = Gson().toJson(_journeyPoints.value.map { LatLngSerializable(it.latitude, it.longitude) })
            journeyDao.updatePath(journey.id, json)
        }
    }


    fun stopJourney() = viewModelScope.launch {
        _currentJourney.value?.let { journey ->
            val endTime = System.currentTimeMillis()
            journeyDao.updateEndTime(journey.id, endTime)
            val pathJson = Gson().toJson(_journeyPoints.value.map { LatLngSerializable(it.latitude, it.longitude) })
            journeyDao.updatePath(journey.id, pathJson)

            // Salvataggio distanza e durata finale
            val distanceMeters = DistanceCalculator.totalDistance(_journeyPoints.value.map { LatLngSerializable(it.latitude, it.longitude) })
            val durationSeconds = (endTime - journey.start) / 1000
            journeyDao.updateDistanceAndDuration(journey.id, distanceMeters, durationSeconds)

            _allJourneyPoints.value = _allJourneyPoints.value + listOf(_journeyPoints.value)

            // Reset
            _currentJourney.value = null
            _journeyPoints.value = emptyList()
            _isJourneyActive.value = false
            _liveDistanceMeters.value = 0.0
            _liveDurationSeconds.value = 0L
        }
    }


    suspend fun loadJourneysForTrip(tripId: Int) {
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
        _journeys.value = journeys
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

    fun deleteJourneysForTrip(tripId: Int) {
        viewModelScope.launch {
            journeyDao.deleteJourneysForTrip(tripId)
            _allJourneyPoints.value = _allJourneyPoints.value.filterNot { false }
            android.util.Log.d("TripVM", "Journey del viaggio $tripId eliminati")
        }
    }

    fun markTripAsCompleted(tripId: Int) {
        viewModelScope.launch {
            dao.markTripAsCompleted(tripId)
        }
    }

    fun checkAndUpdateTripCompletion() {
        viewModelScope.launch {
            val allTrips = dao.getAllTripsOnce()
            val now = LocalDate.now()

            allTrips.forEach { trip ->
                if (!trip.isCompleted && trip.endDate != null && trip.endDate.isBefore(now)) {
                    dao.markTripAsCompleted(trip.id)
                }
            }
        }
    }

    fun loadMemories() {
        viewModelScope.launch {
            val allMarkers = markerDao.getAllPhotosTrip()
            _photos.value = allMarkers.groupBy { it.destination }
        }
    }



}


data class LatLngSerializable(val lat: Double, val lng: Double)