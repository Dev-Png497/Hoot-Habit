package com.hoothabit.app.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hoothabit.app.service.TimerController
import com.hoothabit.app.service.TimerService
import com.hoothabit.app.ui.common.HootPrimaryButton
import com.hoothabit.app.ui.common.HootSecondaryButton
import com.hoothabit.app.ui.common.OwlCompanion
import com.hoothabit.app.ui.common.OwlState
import com.hoothabit.app.ui.common.formatClock
import com.hoothabit.app.ui.theme.LocalHootColors

@Composable
fun TimerScreen(
    habitId: Long,
    targetMinutes: Int,
    accentColor: Color,
    onFinished: (Int) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalHootColors.current
    val timerState by TimerController.state.collectAsState()
    var hasStarted by remember { mutableStateOf(false) }
    var hasCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasStarted) {
            TimerService.start(context, habitId = habitId, targetSeconds = targetMinutes * 60)
            hasStarted = true
        }
    }

    LaunchedEffect(timerState.remainingSeconds, timerState.isActive) {
        if (hasStarted && timerState.isActive && timerState.remainingSeconds == 0 && !hasCompleted) {
            hasCompleted = true
            onFinished(targetMinutes)
        }
    }

    DisposableEffect(Unit) {
        onDispose { }
    }

    val progress = if (timerState.targetSeconds > 0) {
        1f - (timerState.remainingSeconds.toFloat() / timerState.targetSeconds.toFloat())
    } else 0f

    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OwlCompanion(
            state = OwlState.TIMER_ACTIVE,
            accentColor = accentColor,
            bodyColor = colors.surfaceElevated,
            eyeColor = colors.textPrimary,
            size = 110.dp
        )
        Spacer(Modifier.height(32.dp))

        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(220.dp),
                color = accentColor,
                trackColor = colors.divider,
                strokeWidth = 6.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    formatClock(timerState.remainingSeconds),
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.textPrimary
                )
                Text("remaining", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(48.dp))

        Row(horizontalAlignment = Alignment.CenterVertically, verticalAlignment = Alignment.CenterVertically) {
            if (timerState.isPaused) {
                HootPrimaryButton(
                    text = "Resume",
                    onClick = { TimerService.resume(context) },
                    accent = accentColor,
                    onAccent = colors.onAccent,
                    modifier = Modifier.width(140.dp)
                )
            } else {
                HootPrimaryButton(
                    text = "Pause",
                    onClick = { TimerService.pause(context) },
                    accent = accentColor,
                    onAccent = colors.onAccent,
                    modifier = Modifier.width(140.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        HootSecondaryButton(
            text = "Stop & complete",
            onClick = {
                val elapsedMinutes = (timerState.elapsedSeconds / 60).coerceAtLeast(if (timerState.elapsedSeconds > 0) 1 else 0)
                TimerService.stop(context)
                onFinished(elapsedMinutes)
            },
            accent = accentColor
        )
        Spacer(Modifier.height(4.dp))
        HootSecondaryButton(text = "Cancel", onClick = {
            TimerService.stop(context)
            onCancel()
        }, accent = colors.textSecondary)
    }
}
