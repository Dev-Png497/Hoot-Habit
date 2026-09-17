package com.hoothabit.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalTime
import java.time.ZoneId

/**
 * Schedules the two daily reminder slots (spec #13-14) as self-rescheduling
 * one-shot exact alarms — more reliable across doze/reboot than relying on
 * AlarmManager's inexact repeating mode.
 */
object ReminderScheduler {
    const val REQUEST_CODE_REMINDER_1 = 1001
    const val REQUEST_CODE_REMINDER_2 = 1002

    const val EXTRA_MINUTE_OF_DAY = "extra_minute_of_day"
    const val EXTRA_SLOT = "extra_slot"

    fun scheduleDaily(context: Context, minuteOfDay: Int, requestCode: Int) {
        val trigger = nextOccurrence(minuteOfDay, skipToday = false)
        scheduleAt(context, trigger, minuteOfDay, requestCode)
    }

    fun scheduleForTomorrow(context: Context, minuteOfDay: Int, requestCode: Int) {
        val trigger = nextOccurrence(minuteOfDay, skipToday = true)
        scheduleAt(context, trigger, minuteOfDay, requestCode)
    }

    fun cancel(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, requestCode, 0))
    }

    /** Called right after a completion: stop today's reminders, resume tomorrow. */
    fun skipTodayAndResumeTomorrow(context: Context, reminder1Minute: Int?, reminder2Minute: Int?) {
        reminder1Minute?.let {
            cancel(context, REQUEST_CODE_REMINDER_1)
            scheduleForTomorrow(context, it, REQUEST_CODE_REMINDER_1)
        }
        reminder2Minute?.let {
            cancel(context, REQUEST_CODE_REMINDER_2)
            scheduleForTomorrow(context, it, REQUEST_CODE_REMINDER_2)
        }
    }

    private fun scheduleAt(context: Context, triggerAtMillis: Long, minuteOfDay: Int, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context, requestCode, minuteOfDay)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }

    private fun pendingIntent(context: Context, requestCode: Int, minuteOfDay: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_SLOT, requestCode)
            putExtra(EXTRA_MINUTE_OF_DAY, minuteOfDay)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextOccurrence(minuteOfDay: Int, skipToday: Boolean, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        val time = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
        val now = java.time.ZonedDateTime.now(zoneId)
        var target = now.toLocalDate().atTime(time).atZone(zoneId)
        if (skipToday || !target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return target.toInstant().toEpochMilli()
    }
}
