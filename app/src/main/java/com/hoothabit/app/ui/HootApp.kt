package com.hoothabit.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.hoothabit.app.ui.insights.InsightsScreen
import com.hoothabit.app.ui.journey.JourneyScreen
import com.hoothabit.app.ui.settings.SettingsScreen
import com.hoothabit.app.ui.theme.LocalHootColors
import com.hoothabit.app.ui.today.TodayScreen

private data class Tab(val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("Today", Icons.Filled.Today),
    Tab("Journey", Icons.Filled.CalendarMonth),
    Tab("Insights", Icons.Filled.BarChart),
    Tab("Settings", Icons.Filled.Settings)
)

@Composable
fun HootApp() {
    val colors = LocalHootColors.current
    var selected by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            NavigationBar(containerColor = colors.surface, contentColor = colors.textPrimary) {
                TABS.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.textTertiary,
                            unselectedTextColor = colors.textTertiary,
                            indicatorColor = colors.surfaceElevated
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .background(colors.background)
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selected) {
                0 -> TodayScreen(onOpenMilestone = { /* milestone celebration screen added in a later pass */ })
                1 -> JourneyScreen()
                2 -> InsightsScreen()
                3 -> SettingsScreen()
            }
        }
    }
}
