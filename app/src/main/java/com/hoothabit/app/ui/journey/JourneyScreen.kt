package com.hoothabit.app.ui.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoothabit.app.data.db.entity.DayState
import com.hoothabit.app.ui.common.HootCard
import com.hoothabit.app.ui.common.HootViewModelFactory
import com.hoothabit.app.ui.common.TwentyOneDayGrid
import com.hoothabit.app.ui.common.formatDurationMinutes
import com.hoothabit.app.ui.theme.LocalHootColors
import java.time.format.DateTimeFormatter

@Composable
fun JourneyScreen() {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
    val viewModel: JourneyViewModel = viewModel(factory = HootViewModelFactory.from(app))
    val state by viewModel.state.collectAsState()
    val colors = LocalHootColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Journey", style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer2()

        val habit = state.habit
        if (habit != null) {
            HootCard {
                Text(habit.name, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text(
                    "Started ${habit.startDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer2()
            HootCard {
                Text("History", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Spacer2()
                val totalDays = java.time.temporal.ChronoUnit.DAYS.between(habit.startDate, state.today).toInt() + 1
                TwentyOneDayGrid(
                    startDate = habit.startDate,
                    today = state.today,
                    records = state.records,
                    accent = colors.accent,
                    missedColor = colors.missed,
                    protectedColor = colors.protectedMoon,
                    idleColor = colors.divider,
                    backgroundColor = colors.background,
                    totalDays = totalDays.coerceAtLeast(1),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer2()
            if (state.milestones.isNotEmpty()) {
                HootCard {
                    Text("Milestones", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer2()
                    state.milestones.forEach { milestone ->
                        Text(
                            "${milestone.type.replace('_', ' ')} · ${milestone.achievedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                Spacer2()
            }
        }

        if (state.archivedHabits.isNotEmpty()) {
            Text("Archived journeys", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer2()
            state.archivedHabits.forEach { archived ->
                HootCard(modifier = Modifier.padding(bottom = 10.dp)) {
                    Text(archived.name, color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                    val stats = archived.archivedStatsSnapshotJson?.let {
                        runCatching { com.hoothabit.app.domain.StatsSnapshot.fromJson(it) }.getOrNull()
                    }
                    if (stats != null) {
                        Text(
                            "${stats.completedDays} completions · ${formatDurationMinutes(stats.totalMinutesInvested)} · best streak ${stats.bestStreak}",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Spacer2() {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(6.dp))
}
