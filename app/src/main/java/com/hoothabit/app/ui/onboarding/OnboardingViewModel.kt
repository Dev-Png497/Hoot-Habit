package com.hoothabit.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoothabit.app.data.prefs.UserPrefs
import com.hoothabit.app.data.repo.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep {
    WELCOME, HABIT_NAME, COMPLETION_TYPE, MOTIVATION, REMINDERS, DAY_CUTOFF, SUMMARY
}

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val habitName: String = "",
    val isTimed: Boolean = true,
    val dailyTargetMinutes: Int? = 20,
    val motivation: String = "",
    val reminder1Enabled: Boolean = true,
    val reminder1MinuteOfDay: Int = 20 * 60,
    val reminder2Enabled: Boolean = false,
    val reminder2MinuteOfDay: Int = 22 * 60,
    val dayCutoffHour: Int = 0,
    val isSubmitting: Boolean = false,
    val completedHabitId: Long? = null
) {
    val steps = OnboardingStep.entries
    val progress: Float get() = (steps.indexOf(step) + 1f) / steps.size
}

class OnboardingViewModel(
    private val repository: HabitRepository,
    private val userPrefs: UserPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun goTo(step: OnboardingStep) = _state.update { it.copy(step = step) }

    fun next() {
        val order = OnboardingStep.entries
        val idx = order.indexOf(_state.value.step)
        if (idx < order.lastIndex) _state.update { it.copy(step = order[idx + 1]) }
    }

    fun back(): Boolean {
        val order = OnboardingStep.entries
        val idx = order.indexOf(_state.value.step)
        return if (idx > 0) {
            _state.update { it.copy(step = order[idx - 1]) }
            true
        } else false
    }

    fun setHabitName(name: String) = _state.update { it.copy(habitName = name) }
    fun setTimed(isTimed: Boolean) = _state.update { it.copy(isTimed = isTimed, dailyTargetMinutes = if (isTimed) (it.dailyTargetMinutes ?: 20) else null) }
    fun setTargetMinutes(minutes: Int?) = _state.update { it.copy(dailyTargetMinutes = minutes) }
    fun setMotivation(text: String) = _state.update { it.copy(motivation = text) }
    fun setReminder1(enabled: Boolean, minuteOfDay: Int) = _state.update { it.copy(reminder1Enabled = enabled, reminder1MinuteOfDay = minuteOfDay) }
    fun setReminder2(enabled: Boolean, minuteOfDay: Int) = _state.update { it.copy(reminder2Enabled = enabled, reminder2MinuteOfDay = minuteOfDay) }
    fun setDayCutoffHour(hour: Int) = _state.update { it.copy(dayCutoffHour = hour) }

    fun submit() {
        val s = _state.value
        if (s.habitName.isBlank() || s.isSubmitting) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val habitId = repository.createHabit(
                name = s.habitName.trim(),
                motivation = s.motivation.takeIf { it.isNotBlank() },
                isTimed = s.isTimed,
                dailyTargetMinutes = s.dailyTargetMinutes,
                dayCutoffHour = s.dayCutoffHour
            )
            userPrefs.setDayCutoffHour(s.dayCutoffHour)
            userPrefs.setReminder1(s.reminder1Enabled, s.reminder1MinuteOfDay)
            userPrefs.setReminder2(s.reminder2Enabled, s.reminder2MinuteOfDay)
            userPrefs.setOnboardingCompleted(true)
            _state.update { it.copy(isSubmitting = false, completedHabitId = habitId) }
        }
    }
}
