package com.jurobil.progressian.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jurobil.progressian.data.local.converters.Converters
import com.jurobil.progressian.data.local.dao.HabitDao
import com.jurobil.progressian.data.local.dao.MissionDao
import com.jurobil.progressian.data.local.entity.HabitEntity
import com.jurobil.progressian.data.local.entity.MissionEntity

@Database(
    entities = [HabitEntity::class, MissionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ProgressianDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun missionDao(): MissionDao
}