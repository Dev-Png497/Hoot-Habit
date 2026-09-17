package com.hoothabit.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hoothabit.app.data.db.dao.DailyRecordDao
import com.hoothabit.app.data.db.dao.HabitDao
import com.hoothabit.app.data.db.dao.MilestoneDao
import com.hoothabit.app.data.db.dao.NightWatchDao
import com.hoothabit.app.data.db.dao.TimerSessionDao
import com.hoothabit.app.data.db.entity.DailyRecordEntity
import com.hoothabit.app.data.db.entity.HabitEntity
import com.hoothabit.app.data.db.entity.MilestoneEntity
import com.hoothabit.app.data.db.entity.NightWatchEntity
import com.hoothabit.app.data.db.entity.TimerSessionEntity

@Database(
    entities = [
        HabitEntity::class,
        DailyRecordEntity::class,
        TimerSessionEntity::class,
        NightWatchEntity::class,
        MilestoneEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun nightWatchDao(): NightWatchDao
    abstract fun milestoneDao(): MilestoneDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hoot_habit.db"
                ).build().also { instance = it }
            }
        }
    }
}
