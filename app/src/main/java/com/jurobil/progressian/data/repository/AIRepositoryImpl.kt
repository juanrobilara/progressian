package com.jurobil.progressian.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.jurobil.progressian.core.result.Result
import com.jurobil.progressian.data.remote.ai.GeminiPrompts
import com.jurobil.progressian.domain.model.Difficulty
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.HabitFrequency
import com.jurobil.progressian.domain.model.HabitType
import com.jurobil.progressian.domain.model.Mission
import com.jurobil.progressian.domain.repository.AIRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class AIRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel,
) : AIRepository {

    private val gson = Gson()


    override suspend fun generateHabitPlan(userGoal: String): Result<List<Habit>> = withContext(Dispatchers.IO) {
        try {
            val prompt = GeminiPrompts.buildGamificationPrompt(userGoal)
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text

            if (responseText != null) {
                try {
                    val cleanJson = responseText.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val aiResponse = gson.fromJson(cleanJson, AIPlanResponseDto::class.java)
                    val generatedHabits = mapToDomain(aiResponse)

                    Result.Success(generatedHabits)
                } catch (e: Exception) {
                    Log.e("AIRepository", "Error parseando JSON", e)
                    Result.Error(Exception("Error leyendo JSON: ${e.message}"))
                }
            } else {
                Result.Error(Exception("Respuesta vacía de la IA"))
            }
        } catch (e: Exception) {
            Log.e("AIRepository", "Error SDK Gemini", e)
            Result.Error(e)
        }
    }

    override suspend fun generatePixelArt(prompt: String): Result<String> {
        val seed = prompt.replace(" ", "")
        return Result.Success("[https://api.dicebear.com/9.x/pixel-art/svg?seed=$seed](https://api.dicebear.com/9.x/pixel-art/svg?seed=$seed)")
    }


    private data class AIPlanResponseDto(
        val routine: AIRoutineDto,
        val quests: List<AIQuestDto>
    )

    private data class AIRoutineDto(
        val title: String,
        val description: String,
        val xp_reward: Int,
        val daily_missions: List<AIMissionDto>
    )

    private data class AIQuestDto(
        val title: String,
        val description: String,
        val xp_reward: Int,
        val difficulty: String,
        val sub_tasks: List<AIMissionDto>
    )

    private data class AIMissionDto(
        val title: String,
        val description: String,
        val xp: Int,
        val difficulty: String
    )

    private fun mapToDomain(dto: AIPlanResponseDto): List<Habit> {
        val resultList = mutableListOf<Habit>()
        val routineId = UUID.randomUUID().toString()

        val routineHabit = Habit(
            id = routineId,
            parentId = null,
            title = dto.routine.title,
            description = dto.routine.description,
            totalXpReward = dto.routine.xp_reward,
            type = HabitType.ROUTINE,
            frequency = HabitFrequency.DAILY,
            missions = dto.routine.daily_missions.map { m ->
                Mission(
                    id = UUID.randomUUID().toString(),
                    habitId = routineId,
                    title = m.title,
                    description = m.description,
                    difficulty = parseDifficulty(m.difficulty),
                    xpReward = m.xp
                )
            }
        )
        resultList.add(routineHabit)

        dto.quests.forEachIndexed { index, questDto ->
            val questId = UUID.randomUUID().toString()
            val questHabit = Habit(
                id = questId,
                parentId = routineId,
                orderIndex = index,
                title = questDto.title,
                description = questDto.description,
                totalXpReward = questDto.xp_reward,
                type = HabitType.QUEST,
                frequency = HabitFrequency.ONE_TIME,
                missions = questDto.sub_tasks.map { m ->
                    Mission(
                        id = UUID.randomUUID().toString(),
                        habitId = questId,
                        title = m.title,
                        description = m.description,
                        difficulty = parseDifficulty(m.difficulty),
                        xpReward = m.xp
                    )
                }
            )
            resultList.add(questHabit)
        }

        return resultList
    }

    private fun parseDifficulty(diff: String): Difficulty {
        return try {
            Difficulty.valueOf(diff.uppercase())
        } catch (e: Exception) {
            Difficulty.MEDIUM
        }
    }
}