package com.hoothabit.app.domain

import com.hoothabit.app.data.db.entity.DailyRecordEntity
import com.hoothabit.app.data.db.entity.DayState
import java.time.LocalDate

data class StreakInfo(
    val currentStreak: Int,
    val bestStreak: Int,
    val completedDays: Int,
    val protectedDays: Int,
    val missedDays: Int,
    val completionRate: Double // completed / (completed + missed), protected days count as continuity but not "genuine"
)

private enum class DayVerdict { GOOD, BAD, OPEN }

object StreakCalculator {

    fun calculate(
        startDate: LocalDate,
        today: LocalDate,
        records: List<DailyRecordEntity>
    ): StreakInfo {
        if (today.isBefore(startDate)) {
            return StreakInfo(0, 0, 0, 0, 0, 0.0)
        }
        val byDate = records.associateBy { it.habitDate }
        val dates = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .toList()

        val verdicts = dates.map { date ->
            val record = byDate[date]
            when {
                record?.state == DayState.COMPLETED -> DayVerdict.GOOD
                record?.state == DayState.NIGHT_WATCH -> DayVerdict.GOOD
                record?.state == DayState.MISSED -> DayVerdict.BAD
                date == today -> DayVerdict.OPEN
                else -> DayVerdict.BAD // implicit miss: day passed with no record
            }
        }

        var completed = 0
        var protectedDays = 0
        var missed = 0
        for (date in dates) {
            when (byDate[date]?.state) {
                DayState.COMPLETED -> completed++
                DayState.NIGHT_WATCH -> protectedDays++
                DayState.MISSED -> missed++
                null -> if (date != today) missed++
            }
        }

        // Current streak: walk backward from today; an OPEN today is skipped (doesn't break, doesn't count).
        var current = 0
        for (i in verdicts.indices.reversed()) {
            val v = verdicts[i]
            if (i == verdicts.lastIndex && v == DayVerdict.OPEN) continue
            if (v == DayVerdict.GOOD) current++ else break
        }

        // Best streak: longest run of GOOD across the whole history (OPEN never appears mid-run).
        var best = 0
        var run = 0
        for (v in verdicts) {
            if (v == DayVerdict.GOOD) {
                run++
                if (run > best) best = run
            } else {
                run = 0
            }
        }

        val genuineTotal = completed + missed
        val rate = if (genuineTotal > 0) completed.toDouble() / genuineTotal else 0.0

        return StreakInfo(
            currentStreak = current,
            bestStreak = best,
            completedDays = completed,
            protectedDays = protectedDays,
            missedDays = missed,
            completionRate = rate
        )
    }
}
