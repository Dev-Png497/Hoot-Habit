package com.hoothabit.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Handles the "custom habit day end" concept (spec #18): a user can define
 * their habit day to end at 12/1/2/3/4 AM instead of midnight, so a
 * late-night session still counts toward the previous calendar day.
 */
object HabitDayUtils {

    /** Given a real-world instant and the configured cutoff hour, returns which habit-day it belongs to. */
    fun habitDateFor(instant: Instant, cutoffHour: Int, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        val local: LocalDateTime = LocalDateTime.ofInstant(instant, zoneId)
        return if (local.hour < cutoffHour) {
            local.toLocalDate().minusDays(1)
        } else {
            local.toLocalDate()
        }
    }

    fun currentHabitDate(cutoffHour: Int, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        return habitDateFor(Instant.now(), cutoffHour, zoneId)
    }

    /** The wall-clock instant at which [habitDate] ends, given the cutoff hour. */
    fun endOfHabitDay(habitDate: LocalDate, cutoffHour: Int, zoneId: ZoneId = ZoneId.systemDefault()): Instant {
        return habitDate.plusDays(1).atTime(cutoffHour, 0).atZone(zoneId).toInstant()
    }
}
