package com.hoothabit.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class HootColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val accent: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val protectedMoon: Color,
    val missed: Color,
    val success: Color
)

private fun theme(
    background: Color,
    surface: Color,
    surfaceElevated: Color,
    accent: Color
) = HootColors(
    background = background,
    surface = surface,
    surfaceElevated = surfaceElevated,
    accent = accent,
    onAccent = Color(0xFF06070A),
    textPrimary = Color(0xFFF2F2F5),
    textSecondary = Color(0xFFA0A0A8),
    textTertiary = Color(0xFF6B6B72),
    divider = Color(0x1FFFFFFF),
    protectedMoon = Color(0xFFB9C4E0),
    missed = Color(0xFF4A4A50),
    success = accent
)

object HootThemePalettes {
    val Midnight = theme(Color(0xFF0B0B0D), Color(0xFF151518), Color(0xFF1C1C20), Color(0xFF5AC8FA))
    val Forest = theme(Color(0xFF0A0F0C), Color(0xFF131A15), Color(0xFF1A241C), Color(0xFF34C77A))
    val Lavender = theme(Color(0xFF120E1A), Color(0xFF1B1626), Color(0xFF241D30), Color(0xFFB39DDB))
    val Amber = theme(Color(0xFF120E0A), Color(0xFF1D1712), Color(0xFF261E17), Color(0xFFFFB454))
    val Sakura = theme(Color(0xFF170D14), Color(0xFF22141C), Color(0xFF2C1A24), Color(0xFFF2A6C1))
    val Minimal = theme(Color(0xFF141416), Color(0xFF1C1C1F), Color(0xFF25252A), Color(0xFFE8E8EC))
    val Oled = theme(Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFF121212), Color(0xFF5AC8FA))
}

val LocalHootColors = staticCompositionLocalOf { HootThemePalettes.Midnight }
