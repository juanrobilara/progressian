package com.jurobil.progressian.domain.model

data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val totalXpReward: Int,
    val isCompleted: Boolean = false,
    val missions: List<Mission> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)