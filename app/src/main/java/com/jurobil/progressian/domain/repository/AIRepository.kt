package com.jurobil.progressian.domain.repository

import com.jurobil.progressian.core.result.Result
import com.jurobil.progressian.domain.model.Habit

interface AIRepository {
    suspend fun generateHabitPlan(userGoal: String): Result<Habit>
    suspend fun generatePixelArt(prompt: String): Result<String>
}