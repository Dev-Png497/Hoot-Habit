package com.hoothabit.app.domain

import com.hoothabit.app.data.db.entity.NightWatchEntity

data class NightWatchRules(
    val daysToEarn: Int = 14,
    val maxStored: Int = 2,
    val ruleVersion: Int = 1
)

object NightWatchEngine {

    /**
     * Returns true if a brand new Night Watch should be minted right now.
     * Genuine completions accrue toward the next Night Watch; protected
     * (Night Watch) days never count toward earning another one (spec #27).
     */
    fun shouldEarnNewNightWatch(
        totalGenuineCompletions: Int,
        existingNightWatches: List<NightWatchEntity>,
        rules: NightWatchRules = NightWatchRules()
    ): Boolean {
        val entitlement = totalGenuineCompletions / rules.daysToEarn
        val alreadyEarned = existingNightWatches.size
        val available = existingNightWatches.count { it.usedDate == null }
        return entitlement > alreadyEarned && available < rules.maxStored
    }

    fun availableCount(nightWatches: List<NightWatchEntity>): Int =
        nightWatches.count { it.usedDate == null }
}
