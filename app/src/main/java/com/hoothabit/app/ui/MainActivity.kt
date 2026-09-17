package com.hoothabit.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hoothabit.app.HootHabitApp
import com.hoothabit.app.service.TimerService
import com.hoothabit.app.ui.onboarding.OnboardingHost
import com.hoothabit.app.ui.theme.HootHabitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val app = application as HootHabitApp
        TimerService.reconnect(this)

        setContent {
            val settings by app.userPrefs.settings.collectAsState(initial = com.hoothabit.app.data.prefs.UserSettings())
            HootHabitTheme(theme = settings.theme, trueBlackBackgrounds = settings.trueBlackBackgrounds) {
                var onboardingDone by remember(settings.onboardingCompleted) { mutableStateOf(settings.onboardingCompleted) }
                if (!onboardingDone) {
                    OnboardingHost(onFinished = { onboardingDone = true })
                } else {
                    HootApp()
                }
            }
        }
    }
}
