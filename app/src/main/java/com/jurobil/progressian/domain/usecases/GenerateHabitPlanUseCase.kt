package com.jurobil.progressian.domain.usecases

import com.jurobil.progressian.core.result.Result
import com.jurobil.progressian.domain.repository.AIRepository
import com.jurobil.progressian.domain.model.Habit
import javax.inject.Inject

class GenerateHabitPlanUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(userGoal: String): Result<Habit> {
        if (userGoal.isBlank()) {
            return Result.Error(Exception("El objetivo no puede estar vacío"))
        }
        return aiRepository.generateHabitPlan(userGoal)
    }
}