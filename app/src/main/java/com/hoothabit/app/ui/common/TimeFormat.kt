package com.hoothabit.app.ui.common

import java.time.LocalTime

fun formatMinuteOfDay(minuteOfDay: Int): String {
    val hour24 = (minuteOfDay / 60) % 24
    val minute = minuteOfDay % 60
    val amPm = if (hour24 < 12) "AM" else "PM"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return "%d:%02d %s".format(hour12, minute, amPm)
}

fun formatDurationMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

fun formatLocalTime(time: LocalTime): String = formatMinuteOfDay(time.hour * 60 + time.minute)

fun formatClock(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
