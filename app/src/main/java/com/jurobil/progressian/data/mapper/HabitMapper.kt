package com.jurobil.progressian.data.mapper

import com.jurobil.progressian.data.local.entity.HabitEntity
import com.jurobil.progressian.data.local.entity.MissionEntity
import com.jurobil.progressian.data.local.model.HabitWithMissions
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Mission

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl,
        totalXpReward = this.totalXpReward,
        isCompleted = this.isCompleted,
        createdAt = this.createdAt
    )
}

fun Mission.toEntity(habitId: String): MissionEntity {
    return MissionEntity(
        id = this.id,
        habitId = habitId,
        title = this.title,
        description = this.description,
        difficulty = this.difficulty,
        isCompleted = this.isCompleted,
        xpReward = this.xpReward,
        imageUrl = this.imageUrl
    )
}


fun HabitWithMissions.toDomain(): Habit {
    return Habit(
        id = this.habit.id,
        title = this.habit.title,
        description = this.habit.description,
        imageUrl = this.habit.imageUrl,
        totalXpReward = this.habit.totalXpReward,
        isCompleted = this.habit.isCompleted,
        missions = this.missions.map { it.toDomain() },
        createdAt = this.habit.createdAt
    )
}

fun MissionEntity.toDomain(): Mission {
    return Mission(
        id = this.id,
        habitId = this.habitId,
        title = this.title,
        description = this.description,
        difficulty = this.difficulty,
        isCompleted = this.isCompleted,
        xpReward = this.xpReward,
        imageUrl = this.imageUrl
    )
}