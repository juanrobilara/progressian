package com.jurobil.progressian.domain.model

data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val totalXpReward: Int = 0,
    val isCompleted: Boolean = false,
    val missions: List<Mission> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val type: HabitType = HabitType.ROUTINE,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: Long? = null,
    val parentId: String? = null,
    val orderIndex: Int = 0
)