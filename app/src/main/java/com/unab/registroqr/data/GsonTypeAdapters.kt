package com.unab.registroqr.data

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Adaptador de Gson para LocalTime
 */
class LocalTimeAdapter : TypeAdapter<LocalTime>() {
    override fun write(out: JsonWriter, value: LocalTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toString())
        }
    }

    override fun read(`in`: JsonReader): LocalTime? {
        if (`in`.peek() == com.google.gson.stream.JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        return LocalTime.parse(`in`.nextString())
    }
}

/**
 * Adaptador de Gson para DayOfWeek
 */
class DayOfWeekAdapter : TypeAdapter<DayOfWeek>() {
    override fun write(out: JsonWriter, value: DayOfWeek?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.name)
        }
    }

    override fun read(`in`: JsonReader): DayOfWeek? {
        if (`in`.peek() == com.google.gson.stream.JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        return DayOfWeek.valueOf(`in`.nextString())
    }
}
