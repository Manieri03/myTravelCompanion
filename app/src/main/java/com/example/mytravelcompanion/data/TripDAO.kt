package com.example.mytravelcompanion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TripDao {
    @Insert
    suspend fun insertTrip(trip: Trip)

    @Query("SELECT * FROM trips")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips")
    suspend fun getAllTripsOnce(): List<Trip>


    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("UPDATE trips SET isCompleted = 1 WHERE id = :tripId")
    suspend fun markTripAsCompleted(tripId: Int)

    @Query("""
    SELECT * FROM trips 
    WHERE startDate >= :firstDay AND startDate <= :lastDay
""")
    suspend fun getTripsForMonth(firstDay: LocalDate, lastDay: LocalDate): List<Trip>

    @Query("SELECT endDate FROM trips WHERE isCompleted = 1 ORDER BY endDate DESC LIMIT 1")
    suspend fun getLastCompletedTripEndDate(): String?

}