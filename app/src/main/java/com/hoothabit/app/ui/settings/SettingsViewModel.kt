package com.hoothabit.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoothabit.app.data.db.entity.HabitEntity
import com.hoothabit.app.data.prefs.AppTheme
import com.hoothabit.app.data.prefs.UserPrefs
import com.hoothabit.app.data.prefs.UserSettings
import com.hoothabit.app.data.repo.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val habit: HabitEntity? = null,
    val settings: UserSettings = UserSettings()
)

class SettingsViewModel(
    private val repository: HabitRepository,
    private val userPrefs: UserPrefs,
    private val appContext: Context
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        repository.observeActiveHabit(),
        userPrefs.settings
    ) { habit, settings -> SettingsUiState(habit, settings) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(theme: AppTheme) = viewModelScope.launch { userPrefs.setTheme(theme) }
    fun setTrueBlack(enabled: Boolean) = viewModelScope.launch { userPrefs.setTrueBlackBackgrounds(enabled) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { userPrefs.setHapticsEnabled(enabled) }
    fun setSound(enabled: Boolean) = viewModelScope.launch { userPrefs.setSoundEnabled(enabled) }
    fun setReminder1(enabled: Boolean, minuteOfDay: Int) = viewModelScope.launch { userPrefs.setReminder1(enabled, minuteOfDay) }
    fun setDayCutoff(hour: Int) = viewModelScope.launch {
        userPrefs.setDayCutoffHour(hour)
        state.value.habit?.let { repository.updateDayCutoff(it.id, hour) }
    }
    fun setTarget(minutes: Int?) = viewModelScope.launch {
        state.value.habit?.let { repository.updateHabitTarget(it.id, minutes) }
    }
    fun archiveJourney() = viewModelScope.launch {
        state.value.habit?.let { repository.archiveHabit(it.id) }
    }
}
