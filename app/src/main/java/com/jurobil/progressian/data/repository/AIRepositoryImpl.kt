package com.jurobil.progressian.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.jurobil.progressian.core.result.Result
import com.jurobil.progressian.data.remote.ai.GeminiPrompts
import com.jurobil.progressian.domain.model.Difficulty
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Mission
import com.jurobil.progressian.domain.repository.AIRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AIRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel,
) : AIRepository {

    private val gson = Gson()

    override suspend fun generateHabitPlan(userGoal: String): Result<Habit> = withContext(Dispatchers.IO) {
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

                    val aiHabitResponse = gson.fromJson(cleanJson, AIHabitResponseDto::class.java)

                    if (aiHabitResponse.missions.isEmpty()) {
                        return@withContext Result.Error(Exception("La IA no generó misiones."))
                    }

                    val habit = mapToDomain(aiHabitResponse)
                    Result.Success(habit)
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

    private data class AIHabitResponseDto(
        val title: String,
        val description: String,
        val pixel_art_prompt: String,
        val total_xp_reward: Int,
        val missions: List<AIMissionDto>
    )

    private data class AIMissionDto(
        val title: String,
        val description: String,
        val xp: Int,
        val difficulty: String,
        val icon_emoji: String
    )

    private fun mapToDomain(dto: AIHabitResponseDto): Habit {
        val habitId = java.util.UUID.randomUUID().toString()
        return Habit(
            id = habitId,
            title = dto.title,
            description = dto.description,
            imageUrl = null,
            totalXpReward = dto.total_xp_reward,
            missions = dto.missions.map { missionDto ->
                Mission(
                    id = java.util.UUID.randomUUID().toString(),
                    habitId = habitId,
                    title = missionDto.title,
                    description = missionDto.description,
                    difficulty = parseDifficulty(missionDto.difficulty),
                    xpReward = missionDto.xp,
                    imageUrl = null
                )
            }
        )
    }

    private fun parseDifficulty(diff: String): Difficulty {
        return try {
            Difficulty.valueOf(diff.uppercase())
        } catch (e: Exception) {
            Difficulty.MEDIUM
        }
    }

    override suspend fun generatePixelArt(prompt: String): Result<String> {
        val seed = prompt.replace(" ", "")
        return Result.Success("[https://api.dicebear.com/9.x/pixel-art/svg?seed=$seed](https://api.dicebear.com/9.x/pixel-art/svg?seed=$seed)")
    }
}