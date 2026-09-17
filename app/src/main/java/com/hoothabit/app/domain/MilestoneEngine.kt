package com.hoothabit.app.domain

/**
 * Defines every milestone Hoot Habit recognizes and figures out which ones
 * were just crossed. Day-based milestones (7+) are the ones that get a
 * shareable celebration screen (spec #54-55); the others feed Records/Insights.
 */
object MilestoneEngine {

    val DAY_MILESTONES = listOf(7, 14, 21, 30, 50, 75, 100, 150, 200, 250, 365, 730, 1095, 1460, 1825)
    val COMPLETION_MILESTONES = listOf(50, 100, 250, 500, 1000)
    val HOUR_MILESTONES = listOf(10, 25, 50, 100, 250, 500)

    data class Candidate(
        val type: String,
        val threshold: Int,
        val shareEligible: Boolean
    )

    fun findNewMilestones(
        existingTypes: Set<String>,
        dayNumber: Int,
        totalCompletions: Int,
        totalMinutesInvested: Int,
        bestStreak: Int,
        perfectWeeks: Int,
        perfectMonths: Int
    ): List<Candidate> {
        val results = mutableListOf<Candidate>()

        DAY_MILESTONES.firstOrNull { it == dayNumber }?.let { d ->
            val type = "DAY_$d"
            if (type !in existingTypes) results += Candidate(type, d, shareEligible = true)
        }

        COMPLETION_MILESTONES.filter { totalCompletions >= it }.forEach { c ->
            val type = "COMPLETIONS_$c"
            if (type !in existingTypes) results += Candidate(type, c, shareEligible = false)
        }

        val hoursInvested = totalMinutesInvested / 60
        HOUR_MILESTONES.filter { hoursInvested >= it }.forEach { h ->
            val type = "HOURS_$h"
            if (type !in existingTypes) results += Candidate(type, h, shareEligible = false)
        }

        if (bestStreak >= 7) {
            val existingBest = existingTypes
                .filter { it.startsWith("STREAK_RECORD_") }
                .mapNotNull { it.removePrefix("STREAK_RECORD_").toIntOrNull() }
                .maxOrNull() ?: 0
            if (bestStreak > existingBest) {
                results += Candidate("STREAK_RECORD_$bestStreak", bestStreak, shareEligible = false)
            }
        }

        if (perfectWeeks > 0) {
            val type = "PERFECT_WEEKS_$perfectWeeks"
            if (type !in existingTypes) results += Candidate(type, perfectWeeks, shareEligible = false)
        }
        if (perfectMonths > 0) {
            val type = "PERFECT_MONTHS_$perfectMonths"
            if (type !in existingTypes) results += Candidate(type, perfectMonths, shareEligible = false)
        }

        return results
    }
}
