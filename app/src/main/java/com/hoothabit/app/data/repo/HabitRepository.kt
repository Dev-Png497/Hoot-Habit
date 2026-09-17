package com.hoothabit.app.data.repo

import com.hoothabit.app.data.db.dao.DailyRecordDao
import com.hoothabit.app.data.db.dao.HabitDao
import com.hoothabit.app.data.db.dao.MilestoneDao
import com.hoothabit.app.data.db.dao.NightWatchDao
import com.hoothabit.app.data.db.dao.TimerSessionDao
import com.hoothabit.app.data.db.entity.DailyRecordEntity
import com.hoothabit.app.data.db.entity.DayState
import com.hoothabit.app.data.db.entity.HabitEntity
import com.hoothabit.app.data.db.entity.JourneyStatus
import com.hoothabit.app.data.db.entity.MilestoneEntity
import com.hoothabit.app.data.db.entity.NightWatchEntity
import com.hoothabit.app.data.db.entity.SessionSource
import com.hoothabit.app.data.db.entity.TimerSessionEntity
import com.hoothabit.app.domain.HabitDayUtils
import com.hoothabit.app.domain.HabitStats
import com.hoothabit.app.domain.MilestoneEngine
import com.hoothabit.app.domain.NightWatchEngine
import com.hoothabit.app.domain.NightWatchRules
import com.hoothabit.app.domain.StatsCalculator
import com.hoothabit.app.domain.StatsSnapshot
import com.hoothabit.app.domain.StreakCalculator
import com.hoothabit.app.domain.StreakInfo
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class MilestoneAchievement(
    val type: String,
    val threshold: Int,
    val snapshot: StatsSnapshot,
    val shareEligible: Boolean
)

class HabitRepository(
    private val habitDao: HabitDao,
    private val dailyRecordDao: DailyRecordDao,
    private val timerSessionDao: TimerSessionDao,
    private val nightWatchDao: NightWatchDao,
    private val milestoneDao: MilestoneDao
) {
    fun observeActiveHabit(): Flow<HabitEntity?> = habitDao.observeActiveHabit()

    suspend fun getActiveHabit(): HabitEntity? = habitDao.getActiveHabit()

    fun observeArchivedHabits(): Flow<List<HabitEntity>> = habitDao.observeArchivedHabits()

    fun observeAllHabits(): Flow<List<HabitEntity>> = habitDao.observeAllHabits()

    suspend fun getHabit(id: Long): HabitEntity? = habitDao.getById(id)

    fun observeRecords(habitId: Long): Flow<List<DailyRecordEntity>> = dailyRecordDao.observeAllForHabit(habitId)

    fun observeToday(habitId: Long, cutoffHour: Int): Flow<DailyRecordEntity?> {
        val today = HabitDayUtils.currentHabitDate(cutoffHour)
        return dailyRecordDao.observeForDate(habitId, today)
    }

    fun observeNightWatches(habitId: Long): Flow<List<NightWatchEntity>> = nightWatchDao.observeAllForHabit(habitId)

    fun observeMilestones(habitId: Long): Flow<List<MilestoneEntity>> = milestoneDao.observeAllForHabit(habitId)

    suspend fun createHabit(
        name: String,
        motivation: String?,
        isTimed: Boolean,
        dailyTargetMinutes: Int?,
        dayCutoffHour: Int
    ): Long {
        // Only one active habit at a time — archive any existing active journey first.
        habitDao.getActiveHabit()?.let { archiveHabit(it.id) }
        val startDate = HabitDayUtils.currentHabitDate(dayCutoffHour)
        val entity = HabitEntity(
            name = name,
            motivation = motivation,
            startDate = startDate,
            isTimed = isTimed,
            dailyTargetMinutes = dailyTargetMinutes,
            dayCutoffHour = dayCutoffHour,
            isActive = true,
            isArchived = false,
            archivedDate = null,
            foundationCompletedDate = null,
            journeyStatus = JourneyStatus.FOUNDATION,
            createdAt = Instant.now()
        )
        return habitDao.insert(entity)
    }

    suspend fun updateHabitTarget(habitId: Long, dailyTargetMinutes: Int?) {
        val habit = habitDao.getById(habitId) ?: return
        habitDao.update(habit.copy(dailyTargetMinutes = dailyTargetMinutes))
    }

    suspend fun updateDayCutoff(habitId: Long, cutoffHour: Int) {
        val habit = habitDao.getById(habitId) ?: return
        habitDao.update(habit.copy(dayCutoffHour = cutoffHour))
    }

    fun currentHabitDate(cutoffHour: Int): LocalDate = HabitDayUtils.currentHabitDate(cutoffHour)

    fun dayNumberFor(habit: HabitEntity): Int =
        ChronoUnit.DAYS.between(habit.startDate, HabitDayUtils.currentHabitDate(habit.dayCutoffHour)).toInt() + 1

    /**
     * Logs an amount of time (or a simple check-off, minutes=0) toward [date].
     * Sums with any existing sessions for that day; marks the day COMPLETED
     * once the target is met (or immediately for simple/no-goal habits).
     */
    suspend fun logCompletion(
        habit: HabitEntity,
        date: LocalDate,
        minutes: Int,
        source: SessionSource,
        manual: Boolean,
        retroactive: Boolean,
        interrupted: Boolean = false
    ): DailyRecordEntity {
        val now = Instant.now()
        if (minutes > 0) {
            timerSessionDao.insert(
                TimerSessionEntity(
                    habitId = habit.id,
                    habitDate = date,
                    startTime = now.minusSeconds((minutes * 60).toLong()),
                    endTime = now,
                    durationMinutes = minutes,
                    source = source,
                    interrupted = interrupted
                )
            )
        }
        val existing = dailyRecordDao.getForDate(habit.id, date)
        val newTotal = (existing?.totalDurationMinutes ?: 0) + minutes
        val target = habit.dailyTargetMinutes ?: 0
        val reachedTarget = !habit.isTimed || target <= 0 || newTotal >= target
        val record = DailyRecordEntity(
            id = existing?.id ?: 0,
            habitId = habit.id,
            habitDate = date,
            completionTimestamp = if (reachedTarget) (existing?.completionTimestamp ?: now) else existing?.completionTimestamp,
            state = if (reachedTarget) DayState.COMPLETED else (existing?.state ?: DayState.MISSED),
            totalDurationMinutes = newTotal,
            manual = manual || (existing?.manual ?: false),
            retroactive = retroactive || (existing?.retroactive ?: false),
            nightWatch = false,
            note = existing?.note,
            timezoneId = ZoneId.systemDefault().id
        )
        dailyRecordDao.upsert(record)
        return dailyRecordDao.getForDate(habit.id, date)!!
    }

    suspend fun markMissed(habitId: Long, date: LocalDate) {
        val existing = dailyRecordDao.getForDate(habitId, date)
        dailyRecordDao.upsert(
            DailyRecordEntity(
                id = existing?.id ?: 0,
                habitId = habitId,
                habitDate = date,
                completionTimestamp = null,
                state = DayState.MISSED,
                totalDurationMinutes = existing?.totalDurationMinutes ?: 0,
                manual = existing?.manual ?: false,
                retroactive = existing?.retroactive ?: false,
                nightWatch = false,
                note = existing?.note,
                timezoneId = ZoneId.systemDefault().id
            )
        )
    }

    suspend fun getDailyRecord(habitId: Long, date: LocalDate): DailyRecordEntity? =
        dailyRecordDao.getForDate(habitId, date)

    suspend fun getSessionsForDate(habitId: Long, date: LocalDate): List<TimerSessionEntity> =
        timerSessionDao.getForDate(habitId, date)

    suspend fun getAllRecords(habitId: Long): List<DailyRecordEntity> = dailyRecordDao.getAllForHabit(habitId)

    suspend fun getAllSessions(habitId: Long): List<TimerSessionEntity> = timerSessionDao.getAllForHabit(habitId)

    suspend fun getStreakInfo(habit: HabitEntity): StreakInfo {
        val records = dailyRecordDao.getAllForHabit(habit.id)
        val today = HabitDayUtils.currentHabitDate(habit.dayCutoffHour)
        return StreakCalculator.calculate(habit.startDate, today, records)
    }

    suspend fun getStats(habit: HabitEntity): HabitStats {
        val records = dailyRecordDao.getAllForHabit(habit.id)
        val sessions = timerSessionDao.getAllForHabit(habit.id)
        val today = HabitDayUtils.currentHabitDate(habit.dayCutoffHour)
        return StatsCalculator.calculate(habit.startDate, today, habit.dailyTargetMinutes, records, sessions)
    }

    // --- Night Watch ---

    suspend fun getAvailableNightWatches(habitId: Long): List<NightWatchEntity> = nightWatchDao.getAvailable(habitId)

    suspend fun maybeEarnNightWatch(habit: HabitEntity, rules: NightWatchRules) {
        val existing = nightWatchDao.getAllForHabit(habit.id)
        val genuineCompletions = dailyRecordDao.getAllForHabit(habit.id).count { it.state == DayState.COMPLETED }
        if (NightWatchEngine.shouldEarnNewNightWatch(genuineCompletions, existing, rules)) {
            nightWatchDao.insert(
                NightWatchEntity(
                    habitId = habit.id,
                    earnedDate = HabitDayUtils.currentHabitDate(habit.dayCutoffHour),
                    usedDate = null,
                    protectedHabitDate = null,
                    ruleVersion = rules.ruleVersion
                )
            )
        }
    }

    suspend fun useNightWatch(habit: HabitEntity, missedDate: LocalDate): Boolean {
        val available = nightWatchDao.getAvailable(habit.id).firstOrNull() ?: return false
        nightWatchDao.update(available.copy(usedDate = HabitDayUtils.currentHabitDate(habit.dayCutoffHour), protectedHabitDate = missedDate))
        val existing = dailyRecordDao.getForDate(habit.id, missedDate)
        dailyRecordDao.upsert(
            DailyRecordEntity(
                id = existing?.id ?: 0,
                habitId = habit.id,
                habitDate = missedDate,
                completionTimestamp = null,
                state = DayState.NIGHT_WATCH,
                totalDurationMinutes = existing?.totalDurationMinutes ?: 0,
                manual = false,
                retroactive = false,
                nightWatch = true,
                note = existing?.note,
                timezoneId = ZoneId.systemDefault().id
            )
        )
        return true
    }

    // --- Milestones ---

    suspend fun checkAndRecordMilestones(habit: HabitEntity): List<MilestoneAchievement> {
        val records = dailyRecordDao.getAllForHabit(habit.id)
        val today = HabitDayUtils.currentHabitDate(habit.dayCutoffHour)
        val streak = StreakCalculator.calculate(habit.startDate, today, records)
        val dayNumber = dayNumberFor(habit)
        val totalMinutes = records.sumOf { it.totalDurationMinutes }
        val totalCompletions = records.count { it.state == DayState.COMPLETED }
        val stats = StatsCalculator.calculate(habit.startDate, today, habit.dailyTargetMinutes, records, timerSessionDao.getAllForHabit(habit.id))

        val existingTypes = milestoneDao.getAllForHabit(habit.id).map { it.type }.toSet()
        val candidates = MilestoneEngine.findNewMilestones(
            existingTypes = existingTypes,
            dayNumber = dayNumber,
            totalCompletions = totalCompletions,
            totalMinutesInvested = totalMinutes,
            bestStreak = streak.bestStreak,
            perfectWeeks = stats.perfectWeeks,
            perfectMonths = stats.perfectMonths
        )

        val achievements = mutableListOf<MilestoneAchievement>()
        for (candidate in candidates) {
            val snapshot = StatsSnapshot(
                dayNumber = dayNumber,
                currentStreak = streak.currentStreak,
                bestStreak = streak.bestStreak,
                completedDays = streak.completedDays,
                protectedDays = streak.protectedDays,
                completionRatePercent = (streak.completionRate * 100).toInt(),
                totalMinutesInvested = totalMinutes
            )
            milestoneDao.insert(
                MilestoneEntity(
                    habitId = habit.id,
                    type = candidate.type,
                    threshold = candidate.threshold,
                    achievedDate = today,
                    statsSnapshotJson = snapshot.toJson(),
                    shareEligible = candidate.shareEligible,
                    shareCount = 0,
                    shareCardVersion = 1,
                    seen = false
                )
            )
            achievements += MilestoneAchievement(candidate.type, candidate.threshold, snapshot, candidate.shareEligible)
        }

        // Day 21 marks the foundation as complete regardless of a milestone row existing.
        if (dayNumber >= 21 && habit.foundationCompletedDate == null) {
            habitDao.update(habit.copy(foundationCompletedDate = today, journeyStatus = JourneyStatus.CONTINUING))
        }

        return achievements
    }

    suspend fun markMilestoneSeen(milestone: MilestoneEntity) {
        milestoneDao.update(milestone.copy(seen = true))
    }

    suspend fun incrementShareCount(milestone: MilestoneEntity) {
        milestoneDao.update(milestone.copy(shareCount = milestone.shareCount + 1))
    }

    // --- Journey lifecycle ---

    suspend fun setContinuing(habitId: Long) {
        val habit = habitDao.getById(habitId) ?: return
        habitDao.update(habit.copy(journeyStatus = JourneyStatus.CONTINUING))
    }

    suspend fun archiveHabit(habitId: Long) {
        val habit = habitDao.getById(habitId) ?: return
        val stats = getStats(habit)
        val streak = getStreakInfo(habit)
        val snapshot = StatsSnapshot(
            dayNumber = dayNumberFor(habit),
            currentStreak = streak.currentStreak,
            bestStreak = streak.bestStreak,
            completedDays = streak.completedDays,
            protectedDays = streak.protectedDays,
            completionRatePercent = (streak.completionRate * 100).toInt(),
            totalMinutesInvested = stats.totalMinutesInvested
        )
        habitDao.update(
            habit.copy(
                isActive = false,
                isArchived = true,
                archivedDate = HabitDayUtils.currentHabitDate(habit.dayCutoffHour),
                journeyStatus = JourneyStatus.FINISHED,
                archivedStatsSnapshotJson = snapshot.toJson()
            )
        )
    }
}
