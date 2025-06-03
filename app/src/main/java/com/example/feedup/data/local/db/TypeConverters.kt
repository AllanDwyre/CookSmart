package com.example.feedup.data.local.db

import androidx.room.TypeConverter
import com.example.feedup.data.local.entities.Equipment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Gson().fromJson<List<String>>(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromEquipmentList(value: List<Equipment>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toEquipmentList(value: String): List<Equipment> {
        return try {
            Gson().fromJson(value, object : TypeToken<List<Equipment>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}