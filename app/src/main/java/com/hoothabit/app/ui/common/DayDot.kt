package com.hoothabit.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class DotState { COMPLETED, MISSED, PROTECTED, UPCOMING }

/**
 * A single day marker used by the 21-day grid and calendar heatmap.
 * States are distinguished by shape, not just color, for accessibility (spec #20, #78).
 */
@Composable
fun HabitDayDot(
    state: DotState,
    isToday: Boolean,
    accent: Color,
    missedColor: Color,
    protectedColor: Color,
    idleColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        when (state) {
            DotState.COMPLETED -> drawCircle(color = accent, radius = radius, center = center)
            DotState.UPCOMING -> drawCircle(
                color = idleColor,
                radius = radius * 0.85f,
                center = center,
                style = Stroke(width = radius * 0.35f)
            )
            DotState.MISSED -> {
                drawCircle(color = missedColor, radius = radius * 0.85f, center = center, style = Stroke(width = radius * 0.35f))
                val d = radius * 0.6f
                drawLine(
                    color = missedColor,
                    start = Offset(center.x - d, center.y - d),
                    end = Offset(center.x + d, center.y + d),
                    strokeWidth = radius * 0.32f,
                    cap = StrokeCap.Round
                )
            }
            DotState.PROTECTED -> {
                // Crescent moon: a filled disc with an offset disc painted in the
                // background color on top, carving out the crescent shape.
                drawCircle(color = protectedColor, radius = radius * 0.85f, center = center)
                drawCircle(
                    color = backgroundColor,
                    radius = radius * 0.72f,
                    center = Offset(center.x + radius * 0.5f, center.y - radius * 0.18f)
                )
            }
        }
        if (isToday) {
            drawCircle(
                color = accent,
                radius = radius + radius * 0.35f,
                center = center,
                style = Stroke(width = radius * 0.22f)
            )
        }
    }
}
