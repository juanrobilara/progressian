package com.jurobil.progressian.data.repository

import com.jurobil.progressian.data.local.dao.HabitDao
import com.jurobil.progressian.data.local.dao.MissionDao
import com.jurobil.progressian.data.mapper.toDomain
import com.jurobil.progressian.data.mapper.toEntity
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Mission
import com.jurobil.progressian.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao,
    private val missionDao: MissionDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> {
        return dao.getAllHabits().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getHabitById(id: String): Habit? {
        return dao.getHabitById(id)?.toDomain()
    }

    override suspend fun saveHabit(habit: Habit) {
        val habitEntity = habit.toEntity()
        val missionEntities = habit.missions.map { it.toEntity(habitId = habit.id) }
        dao.saveHabitWithMissions(habitEntity, missionEntities)
    }

    override suspend fun toggleMissionComplete(missionId: String, completed: Boolean) {
        dao.updateMissionStatus(missionId, completed)
    }

    override suspend fun toggleMissionIncomplete(missionId: String, completed: Boolean) {
        dao.updateMissionStatus(missionId, !completed)
    }

    override suspend fun getMissionById(missionId: String): Mission? {
        return missionDao.getMissionById(missionId)?.toDomain()
    }

    override suspend fun deleteHabit(habitId: String) {
        dao.deleteHabitById(habitId)
    }
}