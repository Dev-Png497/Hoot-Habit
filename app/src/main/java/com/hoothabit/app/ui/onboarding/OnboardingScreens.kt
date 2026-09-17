package com.hoothabit.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoothabit.app.ui.common.HootPrimaryButton
import com.hoothabit.app.ui.common.HootSecondaryButton
import com.hoothabit.app.ui.common.HootViewModelFactory
import com.hoothabit.app.ui.common.OwlCompanion
import com.hoothabit.app.ui.common.OwlState
import com.hoothabit.app.ui.common.formatMinuteOfDay
import com.hoothabit.app.ui.theme.LocalHootColors

private val SUGGESTED_HABITS = listOf(
    "Read", "Meditate", "Exercise", "Journal", "Study", "Walk", "Stretch", "Practice guitar", "Learn a language", "Drink water"
)

private val TIME_PRESETS = listOf(5, 10, 15, 20, 30, 45, 60)

@Composable
fun OnboardingHost(onFinished: (Long) -> Unit) {
    val viewModel: OnboardingViewModel = viewModel(factory = HootViewModelFactory.from(androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application))
    val state by viewModel.state.collectAsState()
    val colors = LocalHootColors.current

    state.completedHabitId?.let {
        onFinished(it)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.step != OnboardingStep.WELCOME) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.back() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textSecondary
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = colors.accent,
                        trackColor = colors.divider
                    )
                }
            }

            AnimatedContent(
                targetState = state.step,
                modifier = Modifier.fillMaxSize().weight(1f),
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStep(onBegin = { viewModel.next() })
                    OnboardingStep.HABIT_NAME -> HabitNameStep(
                        name = state.habitName,
                        onNameChange = viewModel::setHabitName,
                        onNext = { viewModel.next() }
                    )
                    OnboardingStep.COMPLETION_TYPE -> CompletionTypeStep(
                        isTimed = state.isTimed,
                        targetMinutes = state.dailyTargetMinutes,
                        onTimedChange = viewModel::setTimed,
                        onTargetChange = viewModel::setTargetMinutes,
                        onNext = { viewModel.next() }
                    )
                    OnboardingStep.MOTIVATION -> MotivationStep(
                        motivation = state.motivation,
                        onMotivationChange = viewModel::setMotivation,
                        onNext = { viewModel.next() }
                    )
                    OnboardingStep.REMINDERS -> RemindersStep(
                        reminder1Enabled = state.reminder1Enabled,
                        reminder1Minute = state.reminder1MinuteOfDay,
                        reminder2Enabled = state.reminder2Enabled,
                        reminder2Minute = state.reminder2MinuteOfDay,
                        onReminder1Change = viewModel::setReminder1,
                        onReminder2Change = viewModel::setReminder2,
                        onNext = { viewModel.next() }
                    )
                    OnboardingStep.DAY_CUTOFF -> DayCutoffStep(
                        cutoffHour = state.dayCutoffHour,
                        onCutoffChange = viewModel::setDayCutoffHour,
                        onNext = { viewModel.next() }
                    )
                    OnboardingStep.SUMMARY -> SummaryStep(
                        state = state,
                        isSubmitting = state.isSubmitting,
                        onBegin = { viewModel.submit() }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepScaffold(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit
) {
    val colors = LocalHootColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                title,
                style = MaterialTheme.typography.displayMedium,
                color = colors.textPrimary
            )
            subtitle?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
            }
            Spacer(Modifier.height(28.dp))
            content(this)
        }
        bottomContent()
        Spacer(Modifier.height(36.dp))
    }
}

@Composable
private fun WelcomeStep(onBegin: () -> Unit) {
    val colors = LocalHootColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OwlCompanion(
            state = OwlState.IDLE,
            accentColor = colors.accent,
            bodyColor = colors.surfaceElevated,
            eyeColor = colors.textPrimary,
            size = 160.dp
        )
        Spacer(Modifier.height(28.dp))
        Text(
            "HOOT HABIT",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "One habit.\nStart with 21 days.\nKeep going.",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        HootPrimaryButton(text = "Begin", onClick = onBegin, accent = colors.accent, onAccent = colors.onAccent)
    }
}

@Composable
private fun HabitNameStep(name: String, onNameChange: (String) -> Unit, onNext: () -> Unit) {
    val colors = LocalHootColors.current
    StepScaffold(
        title = "What habit do\nyou want to build?",
        content = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("Read every day", color = colors.textTertiary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(20.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SUGGESTED_HABITS) { suggestion ->
                    SuggestionChip(text = suggestion, selected = name == suggestion) { onNameChange(suggestion) }
                }
            }
        },
        bottomContent = {
            HootPrimaryButton(text = "Continue", onClick = onNext, enabled = name.isNotBlank(), accent = colors.accent, onAccent = colors.onAccent)
        }
    )
}

@Composable
private fun SuggestionChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalHootColors.current
    Box(
        modifier = Modifier
            .background(
                if (selected) colors.accent else colors.surface,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text, color = if (selected) colors.onAccent else colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CompletionTypeStep(
    isTimed: Boolean,
    targetMinutes: Int?,
    onTimedChange: (Boolean) -> Unit,
    onTargetChange: (Int?) -> Unit,
    onNext: () -> Unit
) {
    val colors = LocalHootColors.current
    StepScaffold(
        title = "What counts as\ncompleting it?",
        content = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModeCard("Timed", "Read for 20 minutes", isTimed, Modifier.weight(1f)) { onTimedChange(true) }
                ModeCard("Simple", "No time goal", !isTimed, Modifier.weight(1f)) { onTimedChange(false) }
            }
            if (isTimed) {
                Spacer(Modifier.height(24.dp))
                Text("Daily target", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(TIME_PRESETS) { preset ->
                        SuggestionChip(text = "$preset min", selected = targetMinutes == preset) { onTargetChange(preset) }
                    }
                }
            }
        },
        bottomContent = {
            HootPrimaryButton(text = "Continue", onClick = onNext, accent = colors.accent, onAccent = colors.onAccent)
        }
    )
}

@Composable
private fun ModeCard(title: String, subtitle: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalHootColors.current
    Column(
        modifier = modifier
            .background(if (selected) colors.accent.copy(alpha = 0.15f) else colors.surface, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = if (selected) colors.accent else colors.textPrimary)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    }
}

@Composable
private fun MotivationStep(motivation: String, onMotivationChange: (String) -> Unit, onNext: () -> Unit) {
    val colors = LocalHootColors.current
    StepScaffold(
        title = "Why does this\nmatter to you?",
        subtitle = "Optional — you can skip this.",
        content = {
            OutlinedTextField(
                value = motivation,
                onValueChange = onMotivationChange,
                placeholder = { Text("I want to become someone who reads every night.", color = colors.textTertiary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        bottomContent = {
            Column {
                HootPrimaryButton(text = "Continue", onClick = onNext, accent = colors.accent, onAccent = colors.onAccent)
                HootSecondaryButton(text = "Skip", onClick = onNext, accent = colors.textSecondary)
            }
        }
    )
}

private val TIME_OF_DAY_PRESETS = listOf("Morning" to 8 * 60, "Afternoon" to 14 * 60, "Evening" to 20 * 60)

@Composable
private fun RemindersStep(
    reminder1Enabled: Boolean,
    reminder1Minute: Int,
    reminder2Enabled: Boolean,
    reminder2Minute: Int,
    onReminder1Change: (Boolean, Int) -> Unit,
    onReminder2Change: (Boolean, Int) -> Unit,
    onNext: () -> Unit
) {
    val colors = LocalHootColors.current
    StepScaffold(
        title = "When should\nHoot remind you?",
        content = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TIME_OF_DAY_PRESETS.forEach { (label, minute) ->
                    SuggestionChip(text = label, selected = reminder1Minute == minute) { onReminder1Change(true, minute) }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Reminder at ${formatMinuteOfDay(reminder1Minute)}",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(28.dp))
            Text("If I still haven't completed it...", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SuggestionChip(text = "Remind me again at ${formatMinuteOfDay(reminder2Minute)}", selected = reminder2Enabled) {
                    onReminder2Change(!reminder2Enabled, reminder2Minute)
                }
            }
        },
        bottomContent = {
            HootPrimaryButton(text = "Continue", onClick = onNext, accent = colors.accent, onAccent = colors.onAccent)
        }
    )
}

private val CUTOFF_OPTIONS = listOf(0, 1, 2, 3, 4)

@Composable
private fun DayCutoffStep(cutoffHour: Int, onCutoffChange: (Int) -> Unit, onNext: () -> Unit) {
    val colors = LocalHootColors.current
    StepScaffold(
        title = "When does your\nhabit day end?",
        subtitle = "Late-night sessions before this time still count for the previous day.",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CUTOFF_OPTIONS.forEach { hour ->
                    val label = if (hour == 0) "12:00 AM (midnight)" else "${hour}:00 AM"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (cutoffHour == hour) colors.accent.copy(alpha = 0.15f) else colors.surface, RoundedCornerShape(14.dp))
                            .clickable { onCutoffChange(hour) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, color = if (cutoffHour == hour) colors.accent else colors.textPrimary)
                    }
                }
            }
        },
        bottomContent = {
            HootPrimaryButton(text = "Continue", onClick = onNext, accent = colors.accent, onAccent = colors.onAccent)
        }
    )
}

@Composable
private fun SummaryStep(state: OnboardingState, isSubmitting: Boolean, onBegin: () -> Unit) {
    val colors = LocalHootColors.current
    StepScaffold(
        title = "Ready to\nbegin?",
        subtitle = "Your first 21 days start today. Day 21 is a milestone, not a finish line.",
        content = {
            Spacer(Modifier.height(8.dp))
            SummaryRow("Habit", state.habitName)
            SummaryRow("Goal", state.dailyTargetMinutes?.let { "$it min / day" } ?: "No time goal")
            SummaryRow("Reminder", formatMinuteOfDay(state.reminder1MinuteOfDay))
        },
        bottomContent = {
            HootPrimaryButton(
                text = if (isSubmitting) "Starting…" else "Begin",
                onClick = onBegin,
                enabled = !isSubmitting,
                accent = colors.accent,
                onAccent = colors.onAccent
            )
        }
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val colors = LocalHootColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = colors.textSecondary)
        Text(value, color = colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}
