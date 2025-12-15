package com.jurobil.progressian.domain.model

data class Mission(
    val id: String = java.util.UUID.randomUUID().toString(),
    val habitId: String,
    val title: String,
    val description: String,
    val difficulty: Difficulty,
    val isCompleted: Boolean = false,
    val xpReward: Int,
    val imageUrl: String? = null
)

enum class Difficulty(val xpMultiplier: Double) {
    EASY(1.0), MEDIUM(1.5), HARD(2.0), EPIC(3.0)
}