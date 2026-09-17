package com.hoothabit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.hoothabit.app.data.prefs.AppTheme

fun paletteFor(theme: AppTheme, trueBlack: Boolean): HootColors {
    val base = when (theme) {
        AppTheme.MIDNIGHT -> HootThemePalettes.Midnight
        AppTheme.FOREST -> HootThemePalettes.Forest
        AppTheme.LAVENDER -> HootThemePalettes.Lavender
        AppTheme.AMBER -> HootThemePalettes.Amber
        AppTheme.SAKURA -> HootThemePalettes.Sakura
        AppTheme.MINIMAL -> HootThemePalettes.Minimal
        AppTheme.OLED -> HootThemePalettes.Oled
    }
    return if (trueBlack && theme != AppTheme.OLED) {
        base.copy(background = Color.Black)
    } else {
        base
    }
}

@Composable
fun HootHabitTheme(
    theme: AppTheme = AppTheme.MIDNIGHT,
    trueBlackBackgrounds: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = paletteFor(theme, trueBlackBackgrounds)
    val colorScheme = darkColorScheme(
        primary = palette.accent,
        onPrimary = palette.onAccent,
        secondary = palette.accent,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceElevated,
        onSurfaceVariant = palette.textSecondary,
        outline = palette.divider,
        error = Color(0xFFFF6B6B)
    )

    CompositionLocalProvider(LocalHootColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HootTypography,
            content = content
        )
    }
}
