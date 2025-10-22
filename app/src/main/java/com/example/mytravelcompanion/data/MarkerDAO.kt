package com.example.mytravelcompanion.data

import androidx.room.*

@Dao
interface MarkerDAO {

    @Query("SELECT * FROM markers WHERE tripId = :tripId")
    suspend fun getMarkersByTrip(tripId: Int): List<Marker>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: Marker)

    @Delete
    suspend fun deleteMarker(marker: Marker)

    @Query("DELETE FROM markers WHERE tripId = :tripId")
    suspend fun deleteMarkersByTrip(tripId: Int)

    @Query("""
        SELECT m.id, m.tripId, m.photoPath, t.destination
        FROM markers AS m
        INNER JOIN trips AS t ON m.tripId = t.id
        WHERE m.photoPath IS NOT NULL
        ORDER BY m.tripId
    """)
    suspend fun getAllPhotosTrip(): List<MarkerWithTripName>
}
