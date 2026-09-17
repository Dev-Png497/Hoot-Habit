package com.hoothabit.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoothabit.app.data.prefs.AppTheme
import com.hoothabit.app.ui.common.HootCard
import com.hoothabit.app.ui.common.HootOutlinedButton
import com.hoothabit.app.ui.common.HootViewModelFactory
import com.hoothabit.app.ui.theme.LocalHootColors
import com.hoothabit.app.ui.theme.paletteFor

@Composable
fun SettingsScreen() {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
    val viewModel: SettingsViewModel = viewModel(factory = HootViewModelFactory.from(app))
    val state by viewModel.state.collectAsState()
    val colors = LocalHootColors.current
    var showArchiveConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        state.habit?.let { habit ->
            HootCard {
                Text("Habit", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
                Spacer(Modifier.height(6.dp))
                Text(habit.name, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                habit.dailyTargetMinutes?.let {
                    Text("$it min / day", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        HootCard {
            Text("Theme", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(AppTheme.entries.toList()) { theme ->
                    val palette = paletteFor(theme, false)
                    Column(
                        modifier = Modifier
                            .background(
                                if (state.settings.theme == theme) palette.accent else palette.surface,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.setTheme(theme) }
                            .padding(14.dp)
                    ) {
                        Text(
                            theme.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (state.settings.theme == theme) palette.onAccent else palette.textPrimary
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("True black backgrounds", color = colors.textSecondary)
                Switch(checked = state.settings.trueBlackBackgrounds, onCheckedChange = { viewModel.setTrueBlack(it) })
            }
        }

        Spacer(Modifier.height(16.dp))
        HootCard {
            Text("Feedback", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Haptics", color = colors.textSecondary)
                Switch(checked = state.settings.hapticsEnabled, onCheckedChange = { viewModel.setHaptics(it) })
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sound", color = colors.textSecondary)
                Switch(checked = state.settings.soundEnabled, onCheckedChange = { viewModel.setSound(it) })
            }
        }

        Spacer(Modifier.height(16.dp))
        HootCard {
            Text("Habit day ends at", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(listOf(0, 1, 2, 3, 4)) { hour ->
                    val label = if (hour == 0) "12 AM" else "$hour AM"
                    val selected = state.settings.dayCutoffHour == hour
                    Text(
                        label,
                        color = if (selected) colors.onAccent else colors.textPrimary,
                        modifier = Modifier
                            .background(if (selected) colors.accent else colors.surfaceElevated, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        if (state.habit != null) {
            Spacer(Modifier.height(16.dp))
            HootCard {
                Text("Archive this journey", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Spacer(Modifier.height(6.dp))
                Text("Your history is preserved. You can start a new habit afterward.", color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                if (!showArchiveConfirm) {
                    HootOutlinedButton(text = "Archive journey", onClick = { showArchiveConfirm = true }, accent = colors.textSecondary)
                } else {
                    HootOutlinedButton(text = "Confirm archive", onClick = { viewModel.archiveJourney(); showArchiveConfirm = false }, accent = colors.accent)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
