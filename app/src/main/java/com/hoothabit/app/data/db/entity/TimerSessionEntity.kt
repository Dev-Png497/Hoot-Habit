package com.hoothabit.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

enum class SessionSource {
    TIMER,
    MANUAL
}

@Entity(tableName = "timer_sessions")
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val habitDate: LocalDate,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Int,
    val source: SessionSource,
    val interrupted: Boolean
)
