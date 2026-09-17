package com.hoothabit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hoothabit.app.data.db.entity.NightWatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NightWatchDao {
    @Insert
    suspend fun insert(nightWatch: NightWatchEntity): Long

    @Update
    suspend fun update(nightWatch: NightWatchEntity)

    @Query("SELECT * FROM night_watches WHERE habitId = :habitId ORDER BY earnedDate ASC")
    fun observeAllForHabit(habitId: Long): Flow<List<NightWatchEntity>>

    @Query("SELECT * FROM night_watches WHERE habitId = :habitId ORDER BY earnedDate ASC")
    suspend fun getAllForHabit(habitId: Long): List<NightWatchEntity>

    @Query("SELECT * FROM night_watches WHERE habitId = :habitId AND usedDate IS NULL ORDER BY earnedDate ASC")
    suspend fun getAvailable(habitId: Long): List<NightWatchEntity>

    @Query("DELETE FROM night_watches WHERE habitId = :habitId")
    suspend fun deleteAllForHabit(habitId: Long)
}
