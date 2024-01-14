package com.ncs.o2.HelperClasses

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.firebase.Timestamp
import java.util.Date

class Convertors {

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        if (value == null) {
            return null
        }

        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        if (list == null) {
            return null
        }

        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? {
        return value?.toDate()?.time
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(Date(it)) }
    }
    @TypeConverter
    fun mapfromString(value: String?): Map<String, Any>? {
        if (value == null) {
            return null
        }

        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun maptoString(value: Map<String, Any>?): String? {
        if (value == null) {
            return null
        }

        return Gson().toJson(value)
    }
}
