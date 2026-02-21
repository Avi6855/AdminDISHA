package com.avinashpatil.app.admindisha.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeAdapter : TypeAdapter<LocalTime>() {
    private val writeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val readFormatters = listOf(
        DateTimeFormatter.ISO_LOCAL_TIME,            // HH:mm:ss or HH:mm:ss.SSS
        DateTimeFormatter.ofPattern("HH:mm:ss"),    // explicit seconds
        DateTimeFormatter.ofPattern("HH:mm")        // minutes only
    )

    override fun write(out: JsonWriter, value: LocalTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(writeFormatter.format(value))
        }
    }

    override fun read(reader: JsonReader): LocalTime? {
        return try {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull()
                null
            } else {
                val timeString = reader.nextString()
                if (timeString.isNullOrEmpty()) {
                    null
                } else {
                    var parsed: LocalTime? = null
                    for (f in readFormatters) {
                        try {
                            parsed = LocalTime.parse(timeString, f)
                            break
                        } catch (_: Exception) { /* try next */ }
                    }
                    parsed ?: throw IllegalArgumentException("Unparseable time: $timeString")
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}