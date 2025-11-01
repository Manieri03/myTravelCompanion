package com.example.mytravelcompanion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PointDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: Point): Long

    @Query("SELECT * FROM points")
    suspend fun getAllPoints(): List<Point>

    @Delete
    suspend fun deletePoint(point: Point)

}
