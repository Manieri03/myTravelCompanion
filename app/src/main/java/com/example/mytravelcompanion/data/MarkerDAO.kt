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

    @Query("SELECT * FROM markers WHERE photoPath IS NOT NULL ORDER BY tripId")
    suspend fun getAllPhotos(): List<Marker>
}
