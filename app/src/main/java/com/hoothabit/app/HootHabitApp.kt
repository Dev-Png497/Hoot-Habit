package com.hoothabit.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.hoothabit.app.data.db.AppDatabase
import com.hoothabit.app.data.prefs.UserPrefs
import com.hoothabit.app.data.repo.HabitRepository

class HootHabitApp : Application() {

    lateinit var repository: HabitRepository
        private set
    lateinit var userPrefs: UserPrefs
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = HabitRepository(
            habitDao = db.habitDao(),
            dailyRecordDao = db.dailyRecordDao(),
            timerSessionDao = db.timerSessionDao(),
            nightWatchDao = db.nightWatchDao(),
            milestoneDao = db.milestoneDao()
        )
        userPrefs = UserPrefs(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDERS,
                getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = getString(R.string.notification_channel_reminders_desc) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TIMER,
                getString(R.string.notification_channel_timer),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.notification_channel_timer_desc) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_MILESTONES,
                getString(R.string.notification_channel_milestones),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = getString(R.string.notification_channel_milestones_desc) }
        )
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_TIMER = "timer"
        const val CHANNEL_MILESTONES = "milestones"
    }
}
