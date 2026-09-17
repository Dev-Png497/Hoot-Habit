package com.hoothabit.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoothabit.app.data.db.entity.DayState
import com.hoothabit.app.ui.common.HootPrimaryButton
import com.hoothabit.app.ui.common.HootSecondaryButton
import com.hoothabit.app.ui.common.HootViewModelFactory
import com.hoothabit.app.ui.common.OwlCompanion
import com.hoothabit.app.ui.common.OwlState
import com.hoothabit.app.ui.common.TwentyOneDayGrid
import com.hoothabit.app.ui.common.formatDurationMinutes
import com.hoothabit.app.ui.theme.LocalHootColors
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodayScreen(onOpenMilestone: (com.hoothabit.app.data.repo.MilestoneAchievement) -> Unit) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
    val viewModel: TodayViewModel = viewModel(factory = HootViewModelFactory.from(app))
    val state by viewModel.state.collectAsState()
    val event by viewModel.events.collectAsState()
    val colors = LocalHootColors.current

    var showTimer by remember { mutableStateOf(false) }
    var showRetroactiveDialog by remember { mutableStateOf(false) }
    var showNightWatchDialog by remember { mutableStateOf(false) }
    var showManualDurationDialog by remember { mutableStateOf(false) }
    var lastInsight by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.yesterdayMissingRecord) {
        showRetroactiveDialog = state.yesterdayMissingRecord != null
    }

    LaunchedEffect(event) {
        when (val e = event) {
            is TodayEvent.Completed -> {
                lastInsight = e.insight
                e.milestones.firstOrNull { it.shareEligible }?.let { onOpenMilestone(it) }
                viewModel.consumeEvent()
            }
            TodayEvent.NightWatchUsed, TodayEvent.StreakEnded -> {
                showNightWatchDialog = false
                viewModel.consumeEvent()
            }
            null -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val habit = state.habit
        when {
            state.isLoading -> {}
            habit == null -> EmptyTodayState()
            showTimer -> TimerScreen(
                habitId = habit.id,
                targetMinutes = habit.dailyTargetMinutes ?: 20,
                accentColor = colors.accent,
                onFinished = { minutes ->
                    viewModel.completeFromTimer(minutes)
                    showTimer = false
                },
                onCancel = { showTimer = false }
            )
            else -> TodayContent(
                state = state,
                lastInsight = lastInsight,
                onStart = {
                    if (habit.isTimed) showTimer = true else viewModel.completeSimple()
                },
                onManualComplete = { showManualDurationDialog = true }
            )
        }
    }

    if (showRetroactiveDialog && state.yesterdayMissingRecord != null) {
        RetroactiveDialog(
            onYes = { minutes ->
                viewModel.retroactiveComplete(minutes)
                showRetroactiveDialog = false
            },
            onNo = {
                showRetroactiveDialog = false
                if (state.nightWatchAvailable > 0) showNightWatchDialog = true
                else viewModel.letYesterdayStreakEnd()
            },
            onDismiss = { showRetroactiveDialog = false }
        )
    }

    if (showNightWatchDialog) {
        NightWatchDialog(
            available = state.nightWatchAvailable,
            onUse = { viewModel.useNightWatchForYesterday() },
            onLetEnd = {
                viewModel.letYesterdayStreakEnd()
                showNightWatchDialog = false
            },
            onDismiss = { showNightWatchDialog = false }
        )
    }

    if (showManualDurationDialog) {
        ManualDurationDialog(
            onConfirm = { minutes ->
                viewModel.completeManual(minutes)
                showManualDurationDialog = false
            },
            onDismiss = { showManualDurationDialog = false }
        )
    }
}

@Composable
private fun EmptyTodayState() {
    val colors = LocalHootColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OwlCompanion(OwlState.IDLE, colors.accent, colors.surfaceElevated, colors.textPrimary, size = 120.dp)
        Spacer(Modifier.height(20.dp))
        Text("Ready for something new?", color = colors.textPrimary, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun TodayContent(
    state: TodayUiState,
    lastInsight: String?,
    onStart: () -> Unit,
    onManualComplete: () -> Unit
) {
    val colors = LocalHootColors.current
    val habit = state.habit ?: return
    val dayLabel = state.todayDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "$dayLabel · DAY ${state.dayNumber}",
            style = MaterialTheme.typography.labelLarge,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(28.dp))

        OwlCompanion(
            state = when {
                state.isCompletedToday -> OwlState.RESTING
                else -> OwlState.IDLE
            },
            accentColor = colors.accent,
            bodyColor = colors.surfaceElevated,
            eyeColor = colors.textPrimary,
            size = 130.dp
        )

        Spacer(Modifier.height(28.dp))

        if (state.isCompletedToday) {
            Text("✓ DONE", style = MaterialTheme.typography.headlineMedium, color = colors.accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(formatDurationMinutes(state.minutesLoggedToday), color = colors.textSecondary)
        } else {
            Text(habit.name, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
            habit.dailyTargetMinutes?.let {
                Spacer(Modifier.height(4.dp))
                Text("$it min", color = colors.textSecondary)
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔥", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(8.dp))
            Text("${state.streak.currentStreak} day streak", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }

        lastInsight?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, color = colors.accent, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(36.dp))

        if (!state.isCompletedToday) {
            HootPrimaryButton(
                text = if (habit.isTimed) "Start" else "I did it",
                onClick = onStart,
                accent = colors.accent,
                onAccent = colors.onAccent
            )
            if (habit.isTimed) {
                Spacer(Modifier.height(10.dp))
                HootSecondaryButton(text = "Log it manually", onClick = onManualComplete, accent = colors.textSecondary)
            }
        }

        Spacer(Modifier.height(28.dp))

        if (state.dayNumber <= 21) {
            Text("${minOf(state.dayNumber, 21)} / 21", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            TwentyOneDayGrid(
                startDate = habit.startDate,
                today = state.todayDate,
                records = state.records,
                accent = colors.accent,
                missedColor = colors.missed,
                protectedColor = colors.protectedMoon,
                idleColor = colors.divider,
                backgroundColor = colors.background,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        } else {
            Text("Day ${state.dayNumber}", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
        }

        if (state.nightWatchAvailable > 0) {
            Spacer(Modifier.height(12.dp))
            Text("🌙 ${state.nightWatchAvailable} Night Watch available", color = colors.textTertiary, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun RetroactiveDialog(onYes: (Int) -> Unit, onNo: () -> Unit, onDismiss: () -> Unit) {
    var showDurationStep by remember { mutableStateOf(false) }
    if (!showDurationStep) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Did you actually complete yesterday?") },
            confirmButton = { HootSecondaryButton(text = "Yes, I did", onClick = { showDurationStep = true }) },
            dismissButton = { HootSecondaryButton(text = "No, I missed it", onClick = onNo) }
        )
    } else {
        ManualDurationDialog(onConfirm = onYes, onDismiss = { onYes(0) })
    }
}

@Composable
private fun NightWatchDialog(available: Int, onUse: () -> Unit, onLetEnd: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Protect your streak?") },
        text = { Text("You have $available Night Watch available. Using it protects continuity — yesterday remains missed in your statistics.") },
        confirmButton = { HootSecondaryButton(text = "Use Night Watch", onClick = onUse) },
        dismissButton = { HootSecondaryButton(text = "Let streak end", onClick = onLetEnd) }
    )
}

@Composable
private fun ManualDurationDialog(onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(5, 10, 15, 20, 30, 45, 60)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How long?") },
        text = {
            Column {
                options.forEach { minutes ->
                    HootSecondaryButton(text = "$minutes min", onClick = { onConfirm(minutes) })
                }
                HootSecondaryButton(text = "Skip", onClick = { onConfirm(0) })
            }
        },
        confirmButton = {},
        dismissButton = { HootSecondaryButton(text = "Cancel", onClick = onDismiss) }
    )
}
