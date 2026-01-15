package com.jurobil.progressian.data.local.converters

import androidx.room.TypeConverter
import com.jurobil.progressian.domain.model.Difficulty
import com.jurobil.progressian.domain.model.HabitFrequency
import com.jurobil.progressian.domain.model.HabitType

class Converters {
    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty): String {
        return difficulty.name
    }

    @TypeConverter
    fun toDifficulty(value: String): Difficulty {
        return try {
            Difficulty.valueOf(value)
        } catch (e: Exception) {
            Difficulty.MEDIUM
        }
    }

    @TypeConverter
    fun fromHabitType(type: HabitType): String = type.name

    @TypeConverter
    fun toHabitType(value: String): HabitType = try {
        HabitType.valueOf(value)
    } catch (e: Exception) { HabitType.ROUTINE }

    @TypeConverter
    fun fromHabitFrequency(freq: HabitFrequency): String = freq.name

    @TypeConverter
    fun toHabitFrequency(value: String): HabitFrequency = try {
        HabitFrequency.valueOf(value)
    } catch (e: Exception) { HabitFrequency.DAILY }

}