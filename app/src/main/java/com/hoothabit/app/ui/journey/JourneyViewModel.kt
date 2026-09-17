package com.hoothabit.app.ui.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoothabit.app.data.db.entity.DailyRecordEntity
import com.hoothabit.app.data.db.entity.HabitEntity
import com.hoothabit.app.data.db.entity.MilestoneEntity
import com.hoothabit.app.data.repo.HabitRepository
import com.hoothabit.app.domain.HabitDayUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class JourneyUiState(
    val habit: HabitEntity? = null,
    val today: LocalDate = LocalDate.now(),
    val records: List<DailyRecordEntity> = emptyList(),
    val milestones: List<MilestoneEntity> = emptyList(),
    val archivedHabits: List<HabitEntity> = emptyList()
)

class JourneyViewModel(private val repository: HabitRepository) : ViewModel() {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val state: StateFlow<JourneyUiState> = repository.observeActiveHabit()
        .flatMapLatest { habit ->
            if (habit == null) {
                repository.observeArchivedHabits().let { archivedFlow ->
                    combine(archivedFlow, flowOf(Unit)) { archived, _ ->
                        JourneyUiState(habit = null, archivedHabits = archived)
                    }
                }
            } else {
                combine(
                    repository.observeRecords(habit.id),
                    repository.observeMilestones(habit.id),
                    repository.observeArchivedHabits()
                ) { records, milestones, archived ->
                    JourneyUiState(
                        habit = habit,
                        today = HabitDayUtils.currentHabitDate(habit.dayCutoffHour),
                        records = records,
                        milestones = milestones,
                        archivedHabits = archived
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JourneyUiState())
}
