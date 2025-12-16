package com.jurobil.progressian.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.jurobil.progressian.domain.model.Difficulty
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Mission

fun Habit.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "title" to title,
        "description" to description,
        "imageUrl" to imageUrl,
        "totalXpReward" to totalXpReward,
        "isCompleted" to isCompleted,
        "createdAt" to createdAt,
        "missions" to missions.map { it.toFirestoreMap() }
    )
}

fun Mission.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "habitId" to habitId,
        "title" to title,
        "description" to description,
        "difficulty" to difficulty.name,
        "isCompleted" to isCompleted,
        "xpReward" to xpReward,
        "imageUrl" to imageUrl
    )
}

fun DocumentSnapshot.toDomainHabit(): Habit? {
    try {
        val missionsList = (get("missions") as? List<Map<String, Any>>) ?: emptyList()

        val mappedMissions = missionsList.map { m ->
            Mission(
                id = m["id"] as String,
                habitId = m["habitId"] as String,
                title = m["title"] as String,
                description = m["description"] as String,
                difficulty = Difficulty.valueOf(m["difficulty"] as String),
                isCompleted = m["isCompleted"] as Boolean,
                xpReward = (m["xpReward"] as Long).toInt(),
                imageUrl = m["imageUrl"] as? String
            )
        }

        return Habit(
            id = getString("id") ?: "",
            userId = getString("userId") ?: "",
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            imageUrl = getString("imageUrl"),
            totalXpReward = (getLong("totalXpReward") ?: 0).toInt(),
            isCompleted = getBoolean("isCompleted") ?: false,
            createdAt = getLong("createdAt") ?: 0L,
            missions = mappedMissions
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}