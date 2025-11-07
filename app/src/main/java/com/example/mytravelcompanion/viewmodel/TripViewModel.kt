package com.example.mytravelcompanion.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytravelcompanion.util.DistanceCalculator
import com.example.mytravelcompanion.util.SharedPrefManager
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
                    private val journeyDao: JourneyDAO,
                    private val pointDao: PointDAO) : ViewModel() {

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
    private val _tripsForMonth = MutableStateFlow<List<Trip>>(emptyList())
    val tripsForMonth: StateFlow<List<Trip>> get() = _tripsForMonth


    //Punti di interesse
    private val _points = MutableStateFlow<List<Point>>(emptyList())
    val points: StateFlow<List<Point>> get() = _points


    init {
        checkAndUpdateTripCompletion()
    }

    //Operazioni sui viaggi
    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            dao.insertTrip(trip)
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

    fun markTripAsCompleted(tripId: Int, context: Context) {
        viewModelScope.launch {
            dao.markTripAsCompleted(tripId)
            SharedPrefManager.resetNotified(context)
        }
    }

    fun checkAndUpdateTripCompletion() {
        viewModelScope.launch {
            val now = LocalDate.now()
            trips.collect { allTrips ->
                allTrips.forEach { trip ->
                    if (!trip.isCompleted && trip.endDate != null && trip.endDate.isBefore(now)) {
                        dao.markTripAsCompleted(trip.id)
                    }
                }
            }
        }
    }


    suspend fun getTotalDistanceForTrip(tripId: Int): Double = withContext(Dispatchers.IO) {
        val journeys = journeyDao.getJourneysForTrip(tripId)
        val gson = Gson()
        var totalDistance = 0.0

        for (journey in journeys) {
            if (!journey.path.isNullOrEmpty()) {
                try {
                    val points = gson.fromJson(
                        journey.path,
                        Array<LatLngSerializable>::class.java
                    ).toList()

                    totalDistance += DistanceCalculator.totalDistance(points)
                } catch (e: Exception) {
                    android.util.Log.e("TripVM", "Errore calcolo distanza viaggio ${journey.id}: ${e.message}")
                }
            }
        }

        totalDistance / 1000.0
    }

    fun loadTripsForMonth(firstDay: LocalDate, lastDay: LocalDate) {
        viewModelScope.launch {
            val trips = dao.getTripsForMonth(firstDay, lastDay)
            _tripsForMonth.value = trips
        }
    }


    //Operazioni sui marker (ricordi)
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
    suspend fun deleteMarker(marker: Marker) {
        markerDao.deleteMarker(marker)
    }

    fun loadMemories() {
        viewModelScope.launch {
            val allMarkers = markerDao.getAllPhotosTrip()
            _photos.value = allMarkers.groupBy { it.destination }
        }
    }

    //Operazioni sui percorsi
    fun startJourney(tripId: Int) = viewModelScope.launch {
        _currentJourney.value?.let { current ->
            // salva nel DB
            val json = Gson().toJson(_journeyPoints.value.map {
                LatLngSerializable(it.latitude, it.longitude)
            })
            journeyDao.updatePath(current.id, json)
            journeyDao.updateEndTime(current.id, System.currentTimeMillis())
            android.util.Log.d("TripVM", "Journey precedente salvato (id=${current.id})")

            _allJourneyPoints.value = _allJourneyPoints.value + listOf(_journeyPoints.value)
        }

        val journey = Journey(tripId = tripId, start = System.currentTimeMillis())
        val id = journeyDao.insertJourney(journey)
        _currentJourney.value = journey.copy(id = id)
        _journeyPoints.value = emptyList()
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
            if (delta > 100) {
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


    //Operazioni su punti di interesse
    fun loadPoints(context: Context) = viewModelScope.launch {
        val list = pointDao.getAllPoints()
        _points.value = list
        GeofenceHelper.registerGeofences(context, list)
    }

    fun addPoint(point: Point, context: Context) = viewModelScope.launch {
        pointDao.insertPoint(point)
        _points.value = _points.value + point
        registerGeofence(point, context)
    }

    fun deletePoint(point: Point, context: Context) = viewModelScope.launch {
        pointDao.deletePoint(point)
        _points.value = _points.value.filter { it.name != point.name }
        removeGeofence(point, context)
    }

    //Geofencing
    private fun registerGeofence(point: Point, context: Context) {
        GeofenceHelper.registerGeofences(context, listOf(point))
    }

    private fun removeGeofence(point: Point, context: Context) {
        val geofencingClient = com.google.android.gms.location.LocationServices.getGeofencingClient(context)
        geofencingClient.removeGeofences(listOf(point.name))
    }

}

data class LatLngSerializable(val lat: Double, val lng: Double)
