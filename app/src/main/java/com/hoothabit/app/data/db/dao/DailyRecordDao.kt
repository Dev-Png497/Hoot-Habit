package com.hoothabit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hoothabit.app.data.db.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: DailyRecordEntity): Long

    @Update
    suspend fun update(record: DailyRecordEntity)

    @Query("SELECT * FROM daily_records WHERE habitId = :habitId AND habitDate = :date LIMIT 1")
    suspend fun getForDate(habitId: Long, date: LocalDate): DailyRecordEntity?

    @Query("SELECT * FROM daily_records WHERE habitId = :habitId AND habitDate = :date LIMIT 1")
    fun observeForDate(habitId: Long, date: LocalDate): Flow<DailyRecordEntity?>

    @Query("SELECT * FROM daily_records WHERE habitId = :habitId ORDER BY habitDate ASC")
    fun observeAllForHabit(habitId: Long): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE habitId = :habitId ORDER BY habitDate ASC")
    suspend fun getAllForHabit(habitId: Long): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_records WHERE habitId = :habitId AND habitDate BETWEEN :start AND :end ORDER BY habitDate ASC")
    suspend fun getRange(habitId: Long, start: LocalDate, end: LocalDate): List<DailyRecordEntity>

    @Query("DELETE FROM daily_records WHERE habitId = :habitId")
    suspend fun deleteAllForHabit(habitId: Long)
}
