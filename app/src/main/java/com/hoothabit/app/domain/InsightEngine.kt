package com.hoothabit.app.domain

/**
 * Picks exactly one insight to show after a completion (spec #51). Never a feed —
 * one observation, then the interface settles.
 */
object InsightEngine {

    fun pickPostCompletionInsight(
        before: StreakInfo,
        after: StreakInfo,
        beforeMinutes: Int,
        afterMinutes: Int
    ): String? {
        if (after.currentStreak > before.bestStreak && after.currentStreak >= 2) {
            return "This is your longest streak yet."
        }
        val beforeHours = beforeMinutes / 60
        val afterHours = afterMinutes / 60
        if (afterHours > beforeHours && afterHours > 0) {
            return "${afterHours} hours invested."
        }
        if (after.currentStreak > 0 && after.currentStreak % 7 == 0 && after.currentStreak == after.bestStreak) {
            val weeks = after.currentStreak / 7
            return "$weeks week${if (weeks > 1) "s" else ""}. We've never been this far before."
        }
        if (after.completionRate >= 0.9 && after.completedDays >= 10) {
            return "You're at ${(after.completionRate * 100).toInt()}% consistency."
        }
        return null
    }

    fun beyondTargetInsight(minutesBeyondTarget: Int): String? {
        if (minutesBeyondTarget <= 0) return null
        val hours = minutesBeyondTarget / 60
        val minutes = minutesBeyondTarget % 60
        val formatted = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        return "+$formatted beyond your target."
    }
}
