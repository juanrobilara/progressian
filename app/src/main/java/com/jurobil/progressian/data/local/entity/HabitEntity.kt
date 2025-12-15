package com.jurobil.progressian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val totalXpReward: Int,
    val isCompleted: Boolean,
    val createdAt: Long
)