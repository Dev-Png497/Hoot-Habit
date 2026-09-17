package com.hoothabit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hoothabit.app.data.db.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE isActive = 1 LIMIT 1")
    fun observeActiveHabit(): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveHabit(): HabitEntity?

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY archivedDate DESC")
    fun observeArchivedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeAllHabits(): Flow<List<HabitEntity>>
}
