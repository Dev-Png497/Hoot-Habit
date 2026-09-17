package com.hoothabit.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hoothabit.app.data.db.entity.DailyRecordEntity
import com.hoothabit.app.data.db.entity.DayState
import java.time.LocalDate

@Composable
fun HootPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HootSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = accent, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun HootOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, color = accent)
    }
}

@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = valueColor, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HootCard(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(24.dp))
            .padding(padding),
        content = content
    )
}

/**
 * The signature 21-day foundation grid (spec #20): 3 rows of 7 dots.
 * Shows state via shape (see [HabitDayDot]), never color alone.
 */
@Composable
fun TwentyOneDayGrid(
    startDate: LocalDate,
    today: LocalDate,
    records: List<DailyRecordEntity>,
    accent: Color,
    missedColor: Color,
    protectedColor: Color,
    idleColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    totalDays: Int = 21
) {
    val byDate = records.associateBy { it.habitDate }
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false
    ) {
        items(totalDays) { index ->
            val date = startDate.plusDays(index.toLong())
            val record = byDate[date]
            val state = when {
                record?.state == DayState.COMPLETED -> DotState.COMPLETED
                record?.state == DayState.NIGHT_WATCH -> DotState.PROTECTED
                record?.state == DayState.MISSED -> DotState.MISSED
                date.isAfter(today) -> DotState.UPCOMING
                date.isBefore(today) -> DotState.MISSED
                else -> DotState.UPCOMING // today, not yet logged
            }
            Box(modifier = Modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
                HabitDayDot(
                    state = state,
                    isToday = date == today,
                    accent = accent,
                    missedColor = missedColor,
                    protectedColor = protectedColor,
                    idleColor = idleColor,
                    backgroundColor = backgroundColor,
                    size = 16.dp
                )
            }
        }
    }
}
