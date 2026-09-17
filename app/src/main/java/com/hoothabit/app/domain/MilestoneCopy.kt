package com.hoothabit.app.domain

/** Headline + supporting copy for each milestone type (spec #56-59). Never claims a habit is "formed". */
data class MilestoneCopy(val headline: String, val subtitle: String)

object MilestoneCopyBook {
    fun forType(type: String): MilestoneCopy {
        if (!type.startsWith("DAY_")) {
            return MilestoneCopy("MILESTONE", "Keep going.")
        }
        val day = type.removePrefix("DAY_").toIntOrNull() ?: 0
        return when (day) {
            7 -> MilestoneCopy("ONE WEEK", "7 days of showing up.")
            14 -> MilestoneCopy("TWO WEEKS", "You're building consistency.")
            21 -> MilestoneCopy("21 DAYS", "Your first foundation.")
            30 -> MilestoneCopy("30 DAYS", "A month of showing up.")
            50 -> MilestoneCopy("50 DAYS", "Still going.")
            75 -> MilestoneCopy("75 DAYS", "Built one day at a time.")
            100 -> MilestoneCopy("100 DAYS", "Look what you built.")
            150 -> MilestoneCopy("150 DAYS", "This is who you are now.")
            200 -> MilestoneCopy("200 DAYS", "Still showing up.")
            250 -> MilestoneCopy("250 DAYS", "Look what you built.")
            365 -> MilestoneCopy("ONE YEAR", "365 days of showing up.")
            730 -> MilestoneCopy("TWO YEARS", "Still going.")
            else -> MilestoneCopy("$day DAYS", "Still going.")
        }
    }
}
