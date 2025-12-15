package com.jurobil.progressian.data.local.converters

import androidx.room.TypeConverter
import com.jurobil.progressian.domain.model.Difficulty

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
}