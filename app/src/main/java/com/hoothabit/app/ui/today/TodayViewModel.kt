package com.hoothabit.app.ui.today

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoothabit.app.data.db.entity.DailyRecordEntity
import com.hoothabit.app.data.db.entity.HabitEntity
import com.hoothabit.app.data.db.entity.SessionSource
import com.hoothabit.app.data.prefs.UserPrefs
import com.hoothabit.app.data.prefs.UserSettings
import com.hoothabit.app.data.repo.HabitRepository
import com.hoothabit.app.data.repo.MilestoneAchievement
import com.hoothabit.app.domain.HabitDayUtils
import com.hoothabit.app.domain.HabitStats
import com.hoothabit.app.domain.InsightEngine
import com.hoothabit.app.domain.NightWatchRules
import com.hoothabit.app.domain.StatsCalculator
import com.hoothabit.app.domain.StreakCalculator
import com.hoothabit.app.domain.StreakInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed class TodayEvent {
    data class Completed(val insight: String?, val milestones: List<MilestoneAchievement>) : TodayEvent()
    data object NightWatchUsed : TodayEvent()
    data object StreakEnded : TodayEvent()
}

data class TodayUiState(
    val isLoading: Boolean = true,
    val habit: HabitEntity? = null,
    val settings: UserSettings = UserSettings(),
    val todayRecord: DailyRecordEntity? = null,
    val todayDate: LocalDate = LocalDate.now(),
    val streak: StreakInfo = StreakInfo(0, 0, 0, 0, 0, 0.0),
    val stats: HabitStats? = null,
    val dayNumber: Int = 1,
    val nightWatchAvailable: Int = 0,
    val yesterdayMissingRecord: LocalDate? = null,
    val records: List<DailyRecordEntity> = emptyList()
) {
    val isCompletedToday: Boolean get() = todayRecord?.state == com.hoothabit.app.data.db.entity.DayState.COMPLETED
    val minutesLoggedToday: Int get() = todayRecord?.totalDurationMinutes ?: 0
    val targetMinutes: Int? get() = habit?.dailyTargetMinutes
}

class TodayViewModel(
    private val repository: HabitRepository,
    private val userPrefs: UserPrefs,
    private val appContext: Context
) : ViewModel() {

    private val _events = MutableStateFlow<TodayEvent?>(null)
    val events: StateFlow<TodayEvent?> = _events

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val state: StateFlow<TodayUiState> = repository.observeActiveHabit()
        .flatMapLatest { habit ->
            if (habit == null) {
                flowOf(TodayUiState(isLoading = false, habit = null))
            } else {
                combine(
                    repository.observeRecords(habit.id),
                    userPrefs.settings,
                    repository.observeNightWatches(habit.id)
                ) { records, settings, nightWatches ->
                    val today = HabitDayUtils.currentHabitDate(habit.dayCutoffHour)
                    val streak = StreakCalculator.calculate(habit.startDate, today, records)
                    val stats = StatsCalculator.calculate(habit.startDate, today, habit.dailyTargetMinutes, records, emptyList())
                    val dayNumber = ChronoUnit.DAYS.between(habit.startDate, today).toInt() + 1
                    val todayRecord = records.firstOrNull { it.habitDate == today }
                    val yesterday = today.minusDays(1)
                    val yesterdayMissing = if (!yesterday.isBefore(habit.startDate) && records.none { it.habitDate == yesterday }) {
                        yesterday
                    } else null
                    TodayUiState(
                        isLoading = false,
                        habit = habit,
                        settings = settings,
                        todayRecord = todayRecord,
                        todayDate = today,
                        streak = streak,
                        stats = stats,
                        dayNumber = dayNumber,
                        nightWatchAvailable = nightWatches.count { it.usedDate == null },
                        yesterdayMissingRecord = yesterdayMissing,
                        records = records
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState())

    fun completeSimple() = logAndCelebrate(minutes = 0, manual = true, retroactive = false, date = null)

    fun completeManual(minutes: Int) = logAndCelebrate(minutes = minutes, manual = true, retroactive = false, date = null)

    fun completeFromTimer(minutes: Int) = logAndCelebrate(minutes = minutes, manual = false, retroactive = false, date = null, source = SessionSource.TIMER)

    private fun logAndCelebrate(
        minutes: Int,
        manual: Boolean,
        retroactive: Boolean,
        date: LocalDate?,
        source: SessionSource = SessionSource.MANUAL
    ) {
        val habit = state.value.habit ?: return
        val targetDate = date ?: state.value.todayDate
        val beforeStreak = state.value.streak
        val beforeMinutes = state.value.stats?.totalMinutesInvested ?: 0
        viewModelScope.launch {
            val record = repository.logCompletion(habit, targetDate, minutes, source, manual, retroactive)
            val afterRecords = repository.getAllRecords(habit.id)
            val today = HabitDayUtils.currentHabitDate(habit.dayCutoffHour)
            val afterStreak = StreakCalculator.calculate(habit.startDate, today, afterRecords)
            val afterMinutes = afterRecords.sumOf { it.totalDurationMinutes }
            val settings = state.value.settings
            if (targetDate == today && record.state == com.hoothabit.app.data.db.entity.DayState.COMPLETED) {
                com.hoothabit.app.notifications.ReminderScheduler.skipTodayAndResumeTomorrow(
                    appContext,
                    reminder1Minute = settings.reminder1MinuteOfDay.takeIf { settings.reminder1Enabled },
                    reminder2Minute = settings.reminder2MinuteOfDay.takeIf { settings.reminder2Enabled }
                )
            }
            repository.maybeEarnNightWatch(habit, NightWatchRules(settings.nightWatchDaysToEarn, settings.nightWatchMaxStored))
            val milestones = repository.checkAndRecordMilestones(habit)
            com.hoothabit.app.widget.HootWidget().updateAll(appContext)
            val insight = InsightEngine.pickPostCompletionInsight(beforeStreak, afterStreak, beforeMinutes, afterMinutes)
            _events.value = TodayEvent.Completed(insight, milestones)
        }
    }

    fun retroactiveComplete(minutes: Int) {
        val date = state.value.yesterdayMissingRecord ?: return
        logAndCelebrate(minutes = minutes, manual = true, retroactive = true, date = date)
    }

    fun useNightWatchForYesterday() {
        val habit = state.value.habit ?: return
        val date = state.value.yesterdayMissingRecord ?: return
        viewModelScope.launch {
            val used = repository.useNightWatch(habit, date)
            if (used) _events.value = TodayEvent.NightWatchUsed
        }
    }

    fun letYesterdayStreakEnd() {
        val habit = state.value.habit ?: return
        val date = state.value.yesterdayMissingRecord ?: return
        viewModelScope.launch {
            repository.markMissed(habit.id, date)
            _events.value = TodayEvent.StreakEnded
        }
    }

    fun consumeEvent() {
        _events.value = null
    }

    fun startTimerSession(targetSeconds: Int) {
        val habit = state.value.habit ?: return
        com.hoothabit.app.service.TimerService.start(appContext, habit.id, targetSeconds)
    }
}
