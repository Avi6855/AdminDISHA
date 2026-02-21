package com.avinashpatil.app.admindisha.data.local

import androidx.room.TypeConverter
import com.avinashpatil.app.admindisha.api.Batch
import com.avinashpatil.app.admindisha.api.BatchUsersList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromBatchList(value: List<Batch>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toBatchList(value: String?): List<Batch>? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromBatchUsersList(value: BatchUsersList?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toBatchUsersList(value: String?): BatchUsersList? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(timeFormatter)
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, timeFormatter) }
    }
}
