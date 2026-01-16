package com.jurobil.progressian.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.jurobil.progressian.domain.model.Difficulty
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.HabitFrequency
import com.jurobil.progressian.domain.model.HabitType
import com.jurobil.progressian.domain.model.Mission
import com.jurobil.progressian.domain.model.Post
import com.jurobil.progressian.domain.model.PostType

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
        "type" to type.name,
        "frequency" to frequency.name,
        "parentId" to parentId,
        "orderIndex" to orderIndex,
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
        val data = this.data ?: return null
        return mapToDomainHabit(data)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun mapToDomainHabit(data: Map<String, Any?>): Habit {

    val missionsList = (data["missions"] as? List<Map<String, Any>>) ?: emptyList()
    val mappedMissions = missionsList.map { m ->
        Mission(
            id = m["id"] as? String ?: "",
            habitId = m["habitId"] as? String ?: "",
            title = m["title"] as? String ?: "",
            description = m["description"] as? String ?: "",
            difficulty = try {
                Difficulty.valueOf(m["difficulty"] as String)
            } catch (e: Exception) { Difficulty.MEDIUM },
            isCompleted = m["isCompleted"] as? Boolean ?: false,
            xpReward = (m["xpReward"] as? Long)?.toInt() ?: 10,
            imageUrl = m["imageUrl"] as? String
        )
    }

    return Habit(
        id = data["id"] as? String ?: "",
        userId = data["userId"] as? String ?: "",
        title = data["title"] as? String ?: "",
        description = data["description"] as? String ?: "",
        imageUrl = data["imageUrl"] as? String,
        totalXpReward = (data["totalXpReward"] as? Long)?.toInt() ?: 0,
        isCompleted = data["isCompleted"] as? Boolean ?: false,
        createdAt = data["createdAt"] as? Long ?: 0L,

        type = try {
            HabitType.valueOf(data["type"] as? String ?: "ROUTINE")
        } catch (e: Exception) { HabitType.ROUTINE },

        frequency = try {
            HabitFrequency.valueOf(data["frequency"] as? String ?: "DAILY")
        } catch (e: Exception) { HabitFrequency.DAILY },

        parentId = data["parentId"] as? String,
        orderIndex = (data["orderIndex"] as? Long)?.toInt() ?: 0,
        missions = mappedMissions
    )
}

fun DocumentSnapshot.toPost(): Post? {
    try {
        val data = this.data ?: return null
        val sharedHabitMap = data["sharedHabit"] as? Map<String, Any?>
        val sharedHabit = if (sharedHabitMap != null) {
            mapToDomainHabit(sharedHabitMap)
        } else {
            null
        }

        return Post(
            id = this.id,
            authorId = data["authorId"] as? String ?: "",
            authorName = data["authorName"] as? String ?: "Aventurero",
            content = data["content"] as? String ?: "",
            authorPhotoUrl = data["authorPhotoUrl"] as? String,
            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),

            type = try {
                PostType.valueOf(data["type"] as? String ?: "TEXT")
            } catch (e: Exception) { PostType.TEXT },

            sharedHabit = sharedHabit,

            likeCount = (data["likeCount"] as? Long)?.toInt() ?: 0,
            commentCount = (data["commentCount"] as? Long)?.toInt() ?: 0,
            isLikedByCurrentUser = false
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}