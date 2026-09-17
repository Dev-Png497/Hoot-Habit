package com.hoothabit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.hoothabit.app.data.db.entity.TimerSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TimerSessionDao {
    @Insert
    suspend fun insert(session: TimerSessionEntity): Long

    @Query("SELECT * FROM timer_sessions WHERE habitId = :habitId AND habitDate = :date ORDER BY startTime ASC")
    suspend fun getForDate(habitId: Long, date: LocalDate): List<TimerSessionEntity>

    @Query("SELECT * FROM timer_sessions WHERE habitId = :habitId AND habitDate = :date ORDER BY startTime ASC")
    fun observeForDate(habitId: Long, date: LocalDate): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions WHERE habitId = :habitId ORDER BY startTime ASC")
    suspend fun getAllForHabit(habitId: Long): List<TimerSessionEntity>

    @Query("DELETE FROM timer_sessions WHERE habitId = :habitId")
    suspend fun deleteAllForHabit(habitId: Long)
}
