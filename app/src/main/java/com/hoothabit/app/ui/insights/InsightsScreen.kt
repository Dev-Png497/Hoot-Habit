package com.hoothabit.app.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import com.hoothabit.app.ui.common.HootCard
import com.hoothabit.app.ui.common.HootViewModelFactory
import com.hoothabit.app.ui.common.StatTile
import com.hoothabit.app.ui.common.formatDurationMinutes
import com.hoothabit.app.ui.theme.LocalHootColors

@Composable
fun InsightsScreen() {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
    val viewModel: InsightsViewModel = viewModel(factory = HootViewModelFactory.from(app))
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
        Text("Insights", style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        val streak = state.streak
        val stats = state.stats
        if (streak == null || stats == null) {
            Text("Start a habit to see your insights.", color = colors.textSecondary)
            return@Column
        }

        HootCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatTile(value = "${(stats.completionPercentageLifetime * 100).toInt()}%", label = "completion")
                StatTile(value = "🔥 ${streak.currentStreak}", label = "current streak")
                StatTile(value = formatDurationMinutes(stats.totalMinutesInvested), label = "time invested")
            }
        }
        Spacer(Modifier.height(16.dp))
        HootCard {
            Text("This week vs last 21 days", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatTile(value = "${(stats.completionPercentageLast7 * 100).toInt()}%", label = "last 7 days")
                StatTile(value = "${(stats.completionPercentageLast21 * 100).toInt()}%", label = "last 21 days")
                StatTile(value = "🔥 ${streak.bestStreak}", label = "best streak")
            }
        }
        Spacer(Modifier.height(16.dp))
        HootCard {
            Text("Records", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatTile(value = "${streak.completedDays}", label = "completed days")
                StatTile(value = "${streak.protectedDays}", label = "protected days")
                StatTile(value = "${stats.daysSinceStart}", label = "days since start")
            }
        }
        Spacer(Modifier.height(16.dp))
        HootCard {
            Text("Consistency by weekday", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(12.dp))
            stats.weekdayCompletionRates.entries.sortedBy { it.key.value }.forEach { (day, rate) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(day.name.lowercase().replaceFirstChar { it.uppercase() }, color = colors.textSecondary)
                    Text("${(rate * 100).toInt()}%", color = colors.textPrimary, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
