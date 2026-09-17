package com.hoothabit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hoothabit.app.data.db.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(milestone: MilestoneEntity): Long

    @Update
    suspend fun update(milestone: MilestoneEntity)

    @Query("SELECT * FROM milestones WHERE habitId = :habitId ORDER BY achievedDate DESC")
    fun observeAllForHabit(habitId: Long): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE habitId = :habitId ORDER BY achievedDate DESC")
    suspend fun getAllForHabit(habitId: Long): List<MilestoneEntity>

    @Query("SELECT * FROM milestones WHERE habitId = :habitId AND type = :type LIMIT 1")
    suspend fun getByType(habitId: Long, type: String): MilestoneEntity?

    @Query("SELECT * FROM milestones WHERE habitId = :habitId AND seen = 0 ORDER BY achievedDate ASC")
    fun observeUnseen(habitId: Long): Flow<List<MilestoneEntity>>

    @Query("DELETE FROM milestones WHERE habitId = :habitId")
    suspend fun deleteAllForHabit(habitId: Long)
}
