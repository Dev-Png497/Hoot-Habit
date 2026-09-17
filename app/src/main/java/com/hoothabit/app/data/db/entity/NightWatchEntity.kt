package com.hoothabit.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "night_watches")
data class NightWatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val earnedDate: LocalDate,
    val usedDate: LocalDate?,
    val protectedHabitDate: LocalDate?,
    val ruleVersion: Int
)
