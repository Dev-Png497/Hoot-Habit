package com.hoothabit.app.domain

import com.hoothabit.app.data.db.entity.DailyRecordEntity
import com.hoothabit.app.data.db.entity.DayState
import com.hoothabit.app.data.db.entity.TimerSessionEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class DayPart { MORNING, AFTERNOON, EVENING, NIGHT }

data class HabitStats(
    val daysSinceStart: Int,
    val completionPercentageLifetime: Double,
    val completionPercentageLast7: Double,
    val completionPercentageLast21: Double,
    val completionPercentageLast30: Double,
    val completionPercentageLast90: Double,
    val totalMinutesInvested: Int,
    val minutesThisMonth: Int,
    val averageSessionMinutes: Double,
    val medianSessionMinutes: Double,
    val longestSessionMinutes: Int,
    val shortestSessionMinutes: Int,
    val perfectWeeks: Int,
    val perfectMonths: Int,
    val minutesBeyondTarget: Int,
    val sessionsExceedingTarget: Int,
    val mostCommonCompletionHour: Int?,
    val earliestCompletionTime: LocalTime?,
    val latestCompletionTime: LocalTime?,
    val weekdayCompletionRates: Map<DayOfWeek, Double>,
    val mostConsistentWeekday: DayOfWeek?,
    val leastConsistentWeekday: DayOfWeek?,
    val dayPartCounts: Map<DayPart, Int>
)

object StatsCalculator {

    fun calculate(
        startDate: LocalDate,
        today: LocalDate,
        dailyTargetMinutes: Int?,
        records: List<DailyRecordEntity>,
        sessions: List<TimerSessionEntity>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): HabitStats {
        val daysSinceStart = ChronoUnit.DAYS.between(startDate, today).toInt() + 1
        val completed = records.filter { it.state == DayState.COMPLETED }

        fun completionRateSince(cutoff: LocalDate): Double {
            val windowRecords = records.filter { !it.habitDate.isBefore(cutoff) && !it.habitDate.isAfter(today) }
            val genuine = windowRecords.count { it.state == DayState.COMPLETED || it.state == DayState.MISSED }
            val good = windowRecords.count { it.state == DayState.COMPLETED }
            val windowDays = ChronoUnit.DAYS.between(cutoff, today).toInt() + 1
            // Treat un-recorded past days within the window as missed for an honest percentage.
            val effectiveDenominator = maxOf(genuine, windowDays - 1) // exclude "today" if still open
            return if (effectiveDenominator > 0) good.toDouble() / effectiveDenominator else 0.0
        }

        val totalMinutes = records.sumOf { it.totalDurationMinutes }
        val monthStart = today.withDayOfMonth(1)
        val minutesThisMonth = records.filter { !it.habitDate.isBefore(monthStart) }.sumOf { it.totalDurationMinutes }

        val sessionDurations = sessions.map { it.durationMinutes }.filter { it > 0 }
        val avgSession = if (sessionDurations.isNotEmpty()) sessionDurations.average() else 0.0
        val sortedDurations = sessionDurations.sorted()
        val median = if (sortedDurations.isEmpty()) 0.0 else {
            val mid = sortedDurations.size / 2
            if (sortedDurations.size % 2 == 0) (sortedDurations[mid - 1] + sortedDurations[mid]) / 2.0
            else sortedDurations[mid].toDouble()
        }
        val longest = sessionDurations.maxOrNull() ?: 0
        val shortest = sessionDurations.minOrNull() ?: 0

        val perfectWeeks = countPerfectWeeks(startDate, today, records)
        val perfectMonths = countPerfectMonths(startDate, today, records)

        val target = dailyTargetMinutes ?: 0
        val minutesBeyondTarget = if (target > 0) {
            completed.sumOf { maxOf(0, it.totalDurationMinutes - target) }
        } else 0
        val sessionsExceedingTarget = if (target > 0) sessions.count { it.durationMinutes > target } else 0

        val completionInstants = completed.mapNotNull { it.completionTimestamp }
        val completionTimes = completionInstants.map { LocalTime.from(it.atZone(zoneId)) }
        val hourCounts = completionTimes.groupingBy { it.hour }.eachCount()
        val mostCommonHour = hourCounts.maxByOrNull { it.value }?.key
        val earliest = completionTimes.minOrNull()
        val latest = completionTimes.maxOrNull()

        val weekdayRates = DayOfWeek.entries.associateWith { dow ->
            val onThisWeekday = records.filter { it.habitDate.dayOfWeek == dow && (it.state == DayState.COMPLETED || it.state == DayState.MISSED) }
            val good = onThisWeekday.count { it.state == DayState.COMPLETED }
            if (onThisWeekday.isNotEmpty()) good.toDouble() / onThisWeekday.size else 0.0
        }
        val mostConsistent = weekdayRates.entries.maxByOrNull { it.value }?.key
        val leastConsistent = weekdayRates.entries.minByOrNull { it.value }?.key

        val dayParts = completionTimes.groupingBy { classifyDayPart(it) }.eachCount()
            .let { counts -> DayPart.entries.associateWith { counts[it] ?: 0 } }

        return HabitStats(
            daysSinceStart = daysSinceStart,
            completionPercentageLifetime = completionRateSince(startDate),
            completionPercentageLast7 = completionRateSince(maxOf(startDate, today.minusDays(6))),
            completionPercentageLast21 = completionRateSince(maxOf(startDate, today.minusDays(20))),
            completionPercentageLast30 = completionRateSince(maxOf(startDate, today.minusDays(29))),
            completionPercentageLast90 = completionRateSince(maxOf(startDate, today.minusDays(89))),
            totalMinutesInvested = totalMinutes,
            minutesThisMonth = minutesThisMonth,
            averageSessionMinutes = avgSession,
            medianSessionMinutes = median,
            longestSessionMinutes = longest,
            shortestSessionMinutes = shortest,
            perfectWeeks = perfectWeeks,
            perfectMonths = perfectMonths,
            minutesBeyondTarget = minutesBeyondTarget,
            sessionsExceedingTarget = sessionsExceedingTarget,
            mostCommonCompletionHour = mostCommonHour,
            earliestCompletionTime = earliest,
            latestCompletionTime = latest,
            weekdayCompletionRates = weekdayRates,
            mostConsistentWeekday = mostConsistent,
            leastConsistentWeekday = leastConsistent,
            dayPartCounts = dayParts
        )
    }

    private fun classifyDayPart(time: LocalTime): DayPart = when (time.hour) {
        in 5..11 -> DayPart.MORNING
        in 12..16 -> DayPart.AFTERNOON
        in 17..21 -> DayPart.EVENING
        else -> DayPart.NIGHT
    }

    private fun countPerfectWeeks(startDate: LocalDate, today: LocalDate, records: List<DailyRecordEntity>): Int {
        val byDate = records.associateBy { it.habitDate }
        var count = 0
        var weekStart = startDate.with(DayOfWeek.MONDAY)
        if (weekStart.isAfter(startDate)) weekStart = weekStart.minusWeeks(1)
        while (!weekStart.isAfter(today)) {
            val weekEnd = weekStart.plusDays(6)
            if (weekEnd.isAfter(today)) break // incomplete week
            val daysInRange = (0..6).map { weekStart.plusDays(it.toLong()) }
                .filter { !it.isBefore(startDate) }
            if (daysInRange.isNotEmpty() && daysInRange.all { byDate[it]?.state == DayState.COMPLETED }) {
                count++
            }
            weekStart = weekStart.plusWeeks(1)
        }
        return count
    }

    private fun countPerfectMonths(startDate: LocalDate, today: LocalDate, records: List<DailyRecordEntity>): Int {
        val byDate = records.associateBy { it.habitDate }
        var count = 0
        var monthStart = startDate.withDayOfMonth(1)
        while (!monthStart.isAfter(today)) {
            val monthEnd = monthStart.plusMonths(1).minusDays(1)
            if (monthEnd.isAfter(today)) break // incomplete month
            val daysInRange = generateSequence(monthStart) { it.plusDays(1) }
                .takeWhile { !it.isAfter(monthEnd) }
                .filter { !it.isBefore(startDate) }
                .toList()
            if (daysInRange.isNotEmpty() && daysInRange.all { byDate[it]?.state == DayState.COMPLETED }) {
                count++
            }
            monthStart = monthStart.plusMonths(1)
        }
        return count
    }
}
