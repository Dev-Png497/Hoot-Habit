package com.hoothabit.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hoothabit.app.HootHabitApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Exact alarms don't survive reboot, so re-arm both reminder slots here. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val app = context.applicationContext as HootHabitApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habit = app.repository.getActiveHabit()
                if (habit != null) {
                    val settings = app.userPrefs.settings.first()
                    if (settings.reminder1Enabled) {
                        ReminderScheduler.scheduleDaily(context, settings.reminder1MinuteOfDay, ReminderScheduler.REQUEST_CODE_REMINDER_1)
                    }
                    if (settings.reminder2Enabled) {
                        ReminderScheduler.scheduleDaily(context, settings.reminder2MinuteOfDay, ReminderScheduler.REQUEST_CODE_REMINDER_2)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
