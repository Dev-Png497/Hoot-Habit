package com.hoothabit.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hoothabit.app.HootHabitApp
import com.hoothabit.app.data.db.entity.DayState
import com.hoothabit.app.domain.HabitDayUtils
import com.hoothabit.app.domain.ReminderCopy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Fires for one reminder slot, shows a notification only if today's habit
 * isn't already completed (spec #14: "cancel remaining reminders... once
 * completed" — handled by [ReminderScheduler.skipTodayAndResumeTomorrow]
 * cancelling this alarm outright on completion), then reschedules itself
 * for tomorrow to keep the daily cadence alive without inexact repeating.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slot = intent.getIntExtra(ReminderScheduler.EXTRA_SLOT, ReminderScheduler.REQUEST_CODE_REMINDER_1)
        val minuteOfDay = intent.getIntExtra(ReminderScheduler.EXTRA_MINUTE_OF_DAY, 0)
        val pendingResult = goAsync()
        val app = context.applicationContext as HootHabitApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habit = app.repository.getActiveHabit()
                if (habit != null) {
                    val today = HabitDayUtils.currentHabitDate(habit.dayCutoffHour)
                    val record = app.repository.getDailyRecord(habit.id, today)
                    val alreadyDone = record?.state == DayState.COMPLETED
                    if (!alreadyDone) {
                        val settings = app.userPrefs.settings.first()
                        val dayNumber = app.repository.dayNumberFor(habit)
                        val text = if (slot == ReminderScheduler.REQUEST_CODE_REMINDER_2) {
                            ReminderCopy.secondReminderText(settings.notificationStyle)
                        } else {
                            ReminderCopy.textFor(settings.notificationStyle, habit.name, habit.dailyTargetMinutes, dayNumber)
                        }
                        NotificationHelper.showReminder(context, text, notificationId = slot)
                    }
                }
                ReminderScheduler.scheduleForTomorrow(context, minuteOfDay, slot)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
