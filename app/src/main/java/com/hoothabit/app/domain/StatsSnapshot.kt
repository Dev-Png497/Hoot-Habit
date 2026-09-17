package com.hoothabit.app.domain

import org.json.JSONObject

/** A frozen snapshot of stats at the moment a milestone was achieved (spec #85: "preserve the stats snapshot"). */
data class StatsSnapshot(
    val dayNumber: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val completedDays: Int,
    val protectedDays: Int,
    val completionRatePercent: Int,
    val totalMinutesInvested: Int
) {
    fun toJson(): String = JSONObject().apply {
        put("dayNumber", dayNumber)
        put("currentStreak", currentStreak)
        put("bestStreak", bestStreak)
        put("completedDays", completedDays)
        put("protectedDays", protectedDays)
        put("completionRatePercent", completionRatePercent)
        put("totalMinutesInvested", totalMinutesInvested)
    }.toString()

    companion object {
        fun fromJson(json: String): StatsSnapshot {
            val o = JSONObject(json)
            return StatsSnapshot(
                dayNumber = o.optInt("dayNumber"),
                currentStreak = o.optInt("currentStreak"),
                bestStreak = o.optInt("bestStreak"),
                completedDays = o.optInt("completedDays"),
                protectedDays = o.optInt("protectedDays"),
                completionRatePercent = o.optInt("completionRatePercent"),
                totalMinutesInvested = o.optInt("totalMinutesInvested")
            )
        }
    }
}
