package com.hoothabit.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Placeholder receiver to reschedule reminders after device reboot; wired up in a later pass. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Reminder rescheduling is implemented in a later milestone.
    }
}
