package com.example.mytravelcompanion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface JourneyDAO {

    @Insert
    suspend fun insertJourney(journey: Journey): Long

    @Query("SELECT * FROM journeys WHERE tripId = :tripId")
    suspend fun getJourneysForTrip(tripId: Int): List<Journey>

    @Query("SELECT * FROM journeys WHERE id = :id")
    suspend fun getJourneyById(id: Long): Journey?

    @Query("UPDATE journeys SET end = :endTime WHERE id = :id")
    suspend fun updateEndTime(id: Long, endTime: Long)

    @Query("UPDATE journeys SET path = :pathJson WHERE id = :id")
    suspend fun updatePath(id: Long, pathJson: String)

    @Delete
    suspend fun deleteJourney(journey: Journey)

    @Query("DELETE FROM journeys")
    suspend fun deleteAllJourneys()

    @Query("DELETE FROM journeys WHERE tripId = :tripId")
    suspend fun deleteJourneysForTrip(tripId: Int)

    @Query("SELECT * FROM journeys")
    suspend fun getAllJourneys(): List<Journey>


}