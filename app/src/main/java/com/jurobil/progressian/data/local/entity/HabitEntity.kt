package com.jurobil.progressian.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jurobil.progressian.domain.model.HabitFrequency
import com.jurobil.progressian.domain.model.HabitType

@Entity(
    tableName = "habits",
    indices = [Index(value = ["parentId"])]
)
data class HabitEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val totalXpReward: Int,
    val isCompleted: Boolean,
    val createdAt: Long,
    val type: HabitType = HabitType.ROUTINE,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: Long? = null,
    val parentId: String? = null,
    val orderIndex: Int = 0
)