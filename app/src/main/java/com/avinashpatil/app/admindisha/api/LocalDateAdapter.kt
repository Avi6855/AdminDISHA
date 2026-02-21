package com.avinashpatil.app.admindisha.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter : TypeAdapter<LocalDate>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(reader: JsonReader): LocalDate? {
        return try {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull()
                null
            } else {
                val dateString = reader.nextString()
                if (dateString.isNullOrEmpty()) {
                    null
                } else {
                    LocalDate.parse(dateString, formatter)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}