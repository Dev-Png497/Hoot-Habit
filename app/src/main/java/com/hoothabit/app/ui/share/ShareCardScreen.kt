package com.hoothabit.app.ui.share

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.hoothabit.app.data.prefs.UserSettings
import com.hoothabit.app.data.repo.MilestoneAchievement
import com.hoothabit.app.domain.MilestoneCopyBook
import com.hoothabit.app.ui.common.HootPrimaryButton
import com.hoothabit.app.ui.common.HootSecondaryButton
import com.hoothabit.app.ui.common.formatDurationMinutes
import com.hoothabit.app.ui.theme.LocalHootColors
import java.io.File
import java.io.FileOutputStream

@Composable
fun ShareCardScreen(
    achievement: MilestoneAchievement,
    habitName: String,
    defaultSettings: UserSettings,
    onBack: () -> Unit
) {
    val colors = LocalHootColors.current
    val context = LocalContext.current
    val copy = MilestoneCopyBook.forType(achievement.type)

    var style by remember { mutableStateOf(ShareCardStyle.COMPANION) }
    var format by remember { mutableStateOf(ShareCardFormat.SQUARE) }
    var showHabitName by remember { mutableStateOf(defaultSettings.shareShowHabitName) }
    var showStreak by remember { mutableStateOf(defaultSettings.shareShowStreak) }
    var showCompletion by remember { mutableStateOf(defaultSettings.shareShowCompletion) }
    var showTime by remember { mutableStateOf(defaultSettings.shareShowTime) }
    var showBranding by remember { mutableStateOf(defaultSettings.shareShowBranding) }

    val content = ShareCardContent(
        copy = copy,
        habitName = if (showHabitName) habitName else null,
        bestStreak = if (showStreak) achievement.snapshot.bestStreak else null,
        completionPercent = if (showCompletion) achievement.snapshot.completionRatePercent else null,
        timeInvestedLabel = if (showTime) formatDurationMinutes(achievement.snapshot.totalMinutesInvested) else null,
        showBranding = showBranding
    )

    val bitmap = remember(style, format, content) { ShareCardRenderer.render(format, style, content, colors) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text("Share milestone", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(format.width.toFloat() / format.height.toFloat())
                .background(colors.surface, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Share card preview", modifier = Modifier.fillMaxSize())
        }

        Spacer(Modifier.height(20.dp))
        Text("Style", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShareCardStyle.entries.forEach { s ->
                Chip(text = s.label, selected = style == s) { style = s }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Format", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShareCardFormat.entries.forEach { f ->
                Chip(text = f.label, selected = format == f) { format = f }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("What's shown", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        Spacer(Modifier.height(4.dp))
        ToggleRow("Habit name", showHabitName) { showHabitName = it }
        ToggleRow("Streak", showStreak) { showStreak = it }
        ToggleRow("Completion", showCompletion) { showCompletion = it }
        ToggleRow("Time invested", showTime) { showTime = it }
        ToggleRow("Hoot Habit branding", showBranding) { showBranding = it }

        Spacer(Modifier.height(28.dp))
        HootPrimaryButton(
            text = "Share",
            onClick = {
                val file = File(context.cacheDir, "share_cards").apply { mkdirs() }
                    .resolve("milestone_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out) }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share your milestone"))
            },
            accent = colors.accent,
            onAccent = colors.onAccent
        )
        Spacer(Modifier.height(8.dp))
        HootSecondaryButton(text = "Back", onClick = onBack, accent = colors.textSecondary)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalHootColors.current
    Box(
        modifier = Modifier
            .background(if (selected) colors.accent else colors.surface, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (selected) colors.onAccent else colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = LocalHootColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = colors.textSecondary)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
