package com.hoothabit.app.ui.common

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.hoothabit.app.HootHabitApp
import com.hoothabit.app.ui.insights.InsightsViewModel
import com.hoothabit.app.ui.journey.JourneyViewModel
import com.hoothabit.app.ui.onboarding.OnboardingViewModel
import com.hoothabit.app.ui.settings.SettingsViewModel
import com.hoothabit.app.ui.today.TodayViewModel

/**
 * Minimal manual DI: builds every ViewModel from the singletons held on
 * [HootHabitApp], avoiding a DI framework for a single-module app.
 */
class HootViewModelFactory(private val app: HootHabitApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            OnboardingViewModel::class.java -> OnboardingViewModel(app.repository, app.userPrefs) as T
            TodayViewModel::class.java -> TodayViewModel(app.repository, app.userPrefs, app) as T
            JourneyViewModel::class.java -> JourneyViewModel(app.repository) as T
            InsightsViewModel::class.java -> InsightsViewModel(app.repository) as T
            SettingsViewModel::class.java -> SettingsViewModel(app.repository, app.userPrefs, app) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }

    companion object {
        fun from(application: Application): HootViewModelFactory = HootViewModelFactory(application as HootHabitApp)
    }
}
