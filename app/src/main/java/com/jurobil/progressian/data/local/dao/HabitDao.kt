package com.jurobil.progressian.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jurobil.progressian.data.local.entity.HabitEntity
import com.jurobil.progressian.data.local.entity.MissionEntity
import com.jurobil.progressian.data.local.model.HabitWithMissions
import com.jurobil.progressian.domain.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Transaction
    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllHabits(userId: String): Flow<List<HabitWithMissions>>

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: String): HabitWithMissions?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<MissionEntity>)

    @Transaction
    suspend fun saveHabitWithMissions(habit: HabitEntity, missions: List<MissionEntity>) {
        insertHabit(habit)
        insertMissions(missions)
    }

    @Query("UPDATE missions SET isCompleted = :completed WHERE id = :missionId")
    suspend fun updateMissionStatus(missionId: String, completed: Boolean)
}