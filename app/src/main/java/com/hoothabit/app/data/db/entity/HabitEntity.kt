package com.hoothabit.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

enum class JourneyStatus {
    FOUNDATION, // days 1-21
    CONTINUING, // beyond day 21
    FINISHED    // archived
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val motivation: String?,
    val startDate: LocalDate,
    val isTimed: Boolean,
    val dailyTargetMinutes: Int?, // null for simple (no-time-goal) habits
    val dayCutoffHour: Int, // 0-4, hour of day the "habit day" rolls over
    val isActive: Boolean,
    val isArchived: Boolean,
    val archivedDate: LocalDate?,
    val foundationCompletedDate: LocalDate?,
    val journeyStatus: JourneyStatus,
    val createdAt: Instant,
    // Snapshot of stats taken at archive time so history remains stable.
    val archivedStatsSnapshotJson: String? = null
)
