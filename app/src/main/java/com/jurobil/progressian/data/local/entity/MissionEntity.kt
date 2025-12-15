package com.jurobil.progressian.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jurobil.progressian.domain.model.Difficulty

@Entity(
    tableName = "missions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class MissionEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val title: String,
    val description: String,
    val difficulty: Difficulty,
    val isCompleted: Boolean,
    val xpReward: Int,
    val imageUrl: String?
)