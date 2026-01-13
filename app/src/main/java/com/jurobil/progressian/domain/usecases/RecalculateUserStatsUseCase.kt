package com.jurobil.progressian.domain.usecases

import com.jurobil.progressian.domain.repository.HabitRepository
import com.jurobil.progressian.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RecalculateUserStatsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        val habits = habitRepository.getAllHabits().first()
        val totalRealXp = habits.flatMap { it.missions }
            .filter { it.isCompleted }
            .sumOf { it.xpReward }


        var calculatedLevel = 1
        var remainingXp = totalRealXp
        var xpToNextLevel = 100

        while (remainingXp >= xpToNextLevel) {
            remainingXp -= xpToNextLevel
            calculatedLevel++
            xpToNextLevel = calculatedLevel * 100
        }

        userRepository.forceUpdateStats(calculatedLevel, remainingXp)
    }
}