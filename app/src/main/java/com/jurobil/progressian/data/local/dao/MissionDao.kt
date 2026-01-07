package com.jurobil.progressian.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.jurobil.progressian.data.local.entity.MissionEntity

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun getMissionById(id: String): MissionEntity?

    @Query("UPDATE missions SET title = :title, xpReward = :xpReward WHERE id = :missionId")
    suspend fun updateMissionDetails(missionId: String, title: String, xpReward: Int)
}