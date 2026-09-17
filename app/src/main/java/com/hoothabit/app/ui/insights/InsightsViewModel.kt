package com.hoothabit.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoothabit.app.data.db.entity.HabitEntity
import com.hoothabit.app.data.repo.HabitRepository
import com.hoothabit.app.domain.HabitStats
import com.hoothabit.app.domain.StreakInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InsightsUiState(
    val habit: HabitEntity? = null,
    val streak: StreakInfo? = null,
    val stats: HabitStats? = null,
    val isLoading: Boolean = true
)

class InsightsViewModel(private val repository: HabitRepository) : ViewModel() {

    private val _state = MutableStateFlow(InsightsUiState())
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeActiveHabit().collect { habit ->
                if (habit == null) {
                    _state.value = InsightsUiState(isLoading = false)
                } else {
                    val streak = repository.getStreakInfo(habit)
                    val stats = repository.getStats(habit)
                    _state.value = InsightsUiState(habit = habit, streak = streak, stats = stats, isLoading = false)
                }
            }
        }
    }

    fun refresh() {
        val habit = _state.value.habit ?: return
        viewModelScope.launch {
            val streak = repository.getStreakInfo(habit)
            val stats = repository.getStats(habit)
            _state.value = _state.value.copy(streak = streak, stats = stats)
        }
    }
}
