package com.jurobil.progressian.domain.repository

import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Mission
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>

    suspend fun getHabitById(id: String): Habit?

    suspend fun saveHabit(habit: Habit)

    suspend fun deleteHabit(habitId: String)

    suspend fun toggleMissionComplete(missionId: String, completed: Boolean)

    suspend fun toggleMissionIncomplete(missionId: String, completed: Boolean)

    suspend fun getMissionById(missionId: String): Mission?

    suspend fun syncHabits()
}
