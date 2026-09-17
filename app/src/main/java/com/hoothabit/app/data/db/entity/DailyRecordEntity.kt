package com.hoothabit.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

enum class DayState {
    COMPLETED,
    NIGHT_WATCH,
    MISSED
}

@Entity(
    tableName = "daily_records",
    indices = [Index(value = ["habitId", "habitDate"], unique = true)]
)
data class DailyRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val habitDate: LocalDate,
    val completionTimestamp: Instant?,
    val state: DayState,
    val totalDurationMinutes: Int,
    val manual: Boolean,
    val retroactive: Boolean,
    val nightWatch: Boolean,
    val note: String?,
    val timezoneId: String
)
