package com.example.mytravelcompanion.data

import androidx.room.TypeConverter
import com.example.mytravelcompanion.util.TripType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, formatter) }
    }

    @TypeConverter
    fun fromTripType(type: TripType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTripType(value: String?): TripType? {
        return value?.let { TripType.valueOf(it) }
    }
}
