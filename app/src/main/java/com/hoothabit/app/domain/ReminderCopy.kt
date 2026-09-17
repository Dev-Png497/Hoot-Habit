package com.hoothabit.app.domain

import com.hoothabit.app.data.prefs.NotificationStyle

/** Reminder notification copy per personality (spec #15-16). Never guilt, never fake urgency. */
object ReminderCopy {
    fun textFor(style: NotificationStyle, habitName: String, targetMinutes: Int?, dayNumber: Int): String {
        val goal = targetMinutes?.let { " · $it min" } ?: ""
        return when (style) {
            NotificationStyle.MINIMAL -> "$habitName$goal"
            NotificationStyle.GENTLE -> "Whenever you're ready, today's habit is waiting."
            NotificationStyle.FOCUSED -> "Day $dayNumber. Keep the momentum going."
            NotificationStyle.PLAYFUL -> "Hoot hoot. You know what time it is."
        }
    }

    fun secondReminderText(style: NotificationStyle): String = when (style) {
        NotificationStyle.MINIMAL -> "Still open today."
        NotificationStyle.GENTLE -> "Today is still open. You've got plenty of time."
        NotificationStyle.FOCUSED -> "One more chance to keep today's streak alive."
        NotificationStyle.PLAYFUL -> "Hoot's still up. Today's not over yet."
    }
}
