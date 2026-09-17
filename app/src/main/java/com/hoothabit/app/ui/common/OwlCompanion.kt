package com.hoothabit.app.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.random.Random

enum class OwlState {
    IDLE,
    ATTENTION,
    TIMER_ACTIVE,
    COMPLETION,
    MILESTONE,
    SLEEPY,
    RESTING
}

/**
 * A quiet, hand-drawn owl companion (spec #31-33). It breathes slowly, blinks
 * occasionally, and reacts subtly to state — never needy, never animated
 * constantly.
 */
@Composable
fun OwlCompanion(
    state: OwlState,
    accentColor: Color,
    bodyColor: Color,
    eyeColor: Color,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 140.dp
) {
    val infinite = rememberInfiniteTransition(label = "owl_breathing")
    val breathe by infinite.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedMotion) 1 else 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    var eyeOpenness by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(reducedMotion) {
        if (reducedMotion) return@LaunchedEffect
        while (true) {
            delay(Random.nextLong(2800, 6500))
            eyeOpenness = 0.05f
            delay(120)
            eyeOpenness = 1f
        }
    }

    val wingProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (state == OwlState.MILESTONE) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedMotion) 1 else 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wings"
    )

    val forcedEyeOpenness = when (state) {
        OwlState.COMPLETION, OwlState.RESTING -> 0.2f
        OwlState.SLEEPY -> 0.4f
        else -> eyeOpenness
    }
    val pupilScale = when (state) {
        OwlState.ATTENTION, OwlState.TIMER_ACTIVE -> 1.15f
        OwlState.MILESTONE -> 1.2f
        else -> 1f
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val scale = breathe

        // Wings (only visible when opening for milestones)
        if (state == OwlState.MILESTONE) {
            val wingSpread = wingProgress * w * 0.22f
            rotate(degrees = -wingProgress * 18f, pivot = Offset(cx - w * 0.32f, cy)) {
                drawOval(
                    color = bodyColor,
                    topLeft = Offset(cx - w * 0.5f - wingSpread, cy - h * 0.05f),
                    size = androidx.compose.ui.geometry.Size(w * 0.28f, h * 0.4f)
                )
            }
            rotate(degrees = wingProgress * 18f, pivot = Offset(cx + w * 0.32f, cy)) {
                drawOval(
                    color = bodyColor,
                    topLeft = Offset(cx + w * 0.22f + wingSpread, cy - h * 0.05f),
                    size = androidx.compose.ui.geometry.Size(w * 0.28f, h * 0.4f)
                )
            }
        }

        // Body
        drawOval(
            color = bodyColor,
            topLeft = Offset(cx - (w * 0.31f * scale), cy - (h * 0.32f * scale)),
            size = androidx.compose.ui.geometry.Size(w * 0.62f * scale, h * 0.64f * scale)
        )

        val eyeRadius = min(w, h) * 0.16f
        val eyeY = cy - h * 0.02f
        val eyeDx = w * 0.155f

        listOf(cx - eyeDx, cx + eyeDx).forEach { ex ->
            // Eye white
            drawCircle(color = eyeColor, radius = eyeRadius, center = Offset(ex, eyeY))
            // Eyelid closing from top based on openness
            val lidHeight = eyeRadius * 2 * (1f - forcedEyeOpenness)
            if (lidHeight > 0.5f) {
                drawRect(
                    color = bodyColor,
                    topLeft = Offset(ex - eyeRadius, eyeY - eyeRadius),
                    size = androidx.compose.ui.geometry.Size(eyeRadius * 2, lidHeight)
                )
            }
            // Pupil
            if (forcedEyeOpenness > 0.15f) {
                drawCircle(
                    color = accentColor,
                    radius = eyeRadius * 0.42f * pupilScale,
                    center = Offset(ex, eyeY + eyeRadius * 0.1f)
                )
            }
        }

        // Beak
        val beakY = eyeY + eyeRadius * 1.15f
        val beakPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, beakY)
            lineTo(cx - w * 0.045f, beakY + h * 0.06f)
            lineTo(cx + w * 0.045f, beakY + h * 0.06f)
            close()
        }
        drawPath(beakPath, color = accentColor)

        if (state == OwlState.SLEEPY) {
            // Subtle drooping posture line under the eyes to read as tired.
            drawLine(
                color = accentColor.copy(alpha = 0.25f),
                start = Offset(cx - w * 0.2f, cy + h * 0.28f),
                end = Offset(cx + w * 0.2f, cy + h * 0.28f),
                strokeWidth = 2f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
