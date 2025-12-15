package com.jurobil.progressian.domain.usecases

import com.jurobil.progressian.domain.repository.HabitRepository
import com.jurobil.progressian.domain.repository.UserRepository
import javax.inject.Inject

class CompleteMissionUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(missionId: String, isCompleted: Boolean, xpReward: Int) {
        habitRepository.toggleMissionComplete(missionId, isCompleted)

        if (isCompleted) {
            userRepository.addXp(xpReward)
        }
    }
}