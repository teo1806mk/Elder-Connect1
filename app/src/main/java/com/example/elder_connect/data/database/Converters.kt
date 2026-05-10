package com.example.elder_connect.data.database

import androidx.room.TypeConverter
import com.example.elder_connect.data.entities.MoodType

class Converters {
    @TypeConverter
    fun fromMoodType(value: MoodType): String = value.name

    @TypeConverter
    fun toMoodType(value: String): MoodType = MoodType.valueOf(value)
}
