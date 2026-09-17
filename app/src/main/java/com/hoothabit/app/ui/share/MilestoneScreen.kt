package com.hoothabit.app.ui.share

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hoothabit.app.data.repo.MilestoneAchievement
import com.hoothabit.app.domain.MilestoneCopyBook
import com.hoothabit.app.ui.common.HootPrimaryButton
import com.hoothabit.app.ui.common.HootSecondaryButton
import com.hoothabit.app.ui.common.StatTile
import com.hoothabit.app.ui.common.formatDurationMinutes
import com.hoothabit.app.ui.theme.LocalHootColors

/**
 * The milestone celebration screen (spec #56-59, #29 for Day 21 specifically).
 * Never claims the habit is "formed" — only that a foundation was reached.
 */
@Composable
fun MilestoneScreen(
    achievement: MilestoneAchievement,
    habitName: String,
    onKeepGoing: () -> Unit,
    onShare: () -> Unit
) {
    val colors = LocalHootColors.current
    val copy = MilestoneCopyBook.forType(achievement.type)
    val snapshot = achievement.snapshot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            copy.headline,
            style = MaterialTheme.typography.displayMedium,
            color = colors.accent,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            copy.subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        if (achievement.type == "DAY_21") {
            Spacer(Modifier.height(16.dp))
            Text(
                "There's no single day when a habit automatically becomes permanent. What matters is that you've started building consistency.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(40.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatTile(value = "${snapshot.completedDays}", label = "completed")
            StatTile(value = "${snapshot.completionRatePercent}%", label = "consistency")
        }
        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatTile(value = "🔥 ${snapshot.bestStreak}", label = "best streak")
            StatTile(value = formatDurationMinutes(snapshot.totalMinutesInvested), label = "invested")
        }

        Spacer(Modifier.height(48.dp))

        HootPrimaryButton(text = "Keep going", onClick = onKeepGoing, accent = colors.accent, onAccent = colors.onAccent)
        if (achievement.shareEligible) {
            Spacer(Modifier.height(8.dp))
            HootSecondaryButton(text = "Share milestone", onClick = onShare, accent = colors.textSecondary)
        }
    }
}
