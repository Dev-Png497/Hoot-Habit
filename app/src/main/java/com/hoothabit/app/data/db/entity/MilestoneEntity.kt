package com.hoothabit.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "milestones",
    indices = [Index(value = ["habitId", "type"], unique = true)]
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val type: String, // e.g. "DAY_7", "DAY_21", "STREAK_RECORD_13", "HOURS_10", "COMPLETIONS_50"
    val threshold: Int,
    val achievedDate: LocalDate,
    // Snapshot of stats at the moment of achievement so old share cards stay accurate.
    val statsSnapshotJson: String,
    val shareEligible: Boolean,
    val shareCount: Int,
    val shareCardVersion: Int,
    val seen: Boolean
)
