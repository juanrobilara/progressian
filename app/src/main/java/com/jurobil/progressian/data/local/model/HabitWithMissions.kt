package com.jurobil.progressian.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.jurobil.progressian.data.local.entity.HabitEntity
import com.jurobil.progressian.data.local.entity.MissionEntity

data class HabitWithMissions(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val missions: List<MissionEntity>
)