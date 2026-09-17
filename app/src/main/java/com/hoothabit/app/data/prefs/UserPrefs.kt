package com.hoothabit.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "hoot_habit_prefs")

enum class AppTheme { MIDNIGHT, FOREST, LAVENDER, AMBER, SAKURA, MINIMAL, OLED }

enum class NotificationStyle { MINIMAL, GENTLE, FOCUSED, PLAYFUL }

data class UserSettings(
    val onboardingCompleted: Boolean = false,
    val theme: AppTheme = AppTheme.MIDNIGHT,
    val trueBlackBackgrounds: Boolean = false,
    val notificationStyle: NotificationStyle = NotificationStyle.GENTLE,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val dayCutoffHour: Int = 0,
    val reminder1Enabled: Boolean = true,
    val reminder1MinuteOfDay: Int = 20 * 60, // 8:00 PM
    val reminder2Enabled: Boolean = false,
    val reminder2MinuteOfDay: Int = 22 * 60, // 10:00 PM
    val shareShowHabitName: Boolean = true,
    val shareShowStreak: Boolean = true,
    val shareShowCompletion: Boolean = true,
    val shareShowTime: Boolean = true,
    val shareShowBranding: Boolean = true,
    val nightWatchDaysToEarn: Int = 14,
    val nightWatchMaxStored: Int = 2,
    val reducedMotion: Boolean = false
)

class UserPrefs(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME = stringPreferencesKey("theme")
        val TRUE_BLACK = booleanPreferencesKey("true_black_backgrounds")
        val NOTIFICATION_STYLE = stringPreferencesKey("notification_style")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DAY_CUTOFF_HOUR = intPreferencesKey("day_cutoff_hour")
        val REMINDER1_ENABLED = booleanPreferencesKey("reminder1_enabled")
        val REMINDER1_MINUTE = intPreferencesKey("reminder1_minute")
        val REMINDER2_ENABLED = booleanPreferencesKey("reminder2_enabled")
        val REMINDER2_MINUTE = intPreferencesKey("reminder2_minute")
        val SHARE_HABIT_NAME = booleanPreferencesKey("share_habit_name")
        val SHARE_STREAK = booleanPreferencesKey("share_streak")
        val SHARE_COMPLETION = booleanPreferencesKey("share_completion")
        val SHARE_TIME = booleanPreferencesKey("share_time")
        val SHARE_BRANDING = booleanPreferencesKey("share_branding")
        val NIGHT_WATCH_DAYS = intPreferencesKey("night_watch_days_to_earn")
        val NIGHT_WATCH_MAX = intPreferencesKey("night_watch_max_stored")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            theme = prefs[Keys.THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.MIDNIGHT,
            trueBlackBackgrounds = prefs[Keys.TRUE_BLACK] ?: false,
            notificationStyle = prefs[Keys.NOTIFICATION_STYLE]?.let { runCatching { NotificationStyle.valueOf(it) }.getOrNull() } ?: NotificationStyle.GENTLE,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            dayCutoffHour = prefs[Keys.DAY_CUTOFF_HOUR] ?: 0,
            reminder1Enabled = prefs[Keys.REMINDER1_ENABLED] ?: true,
            reminder1MinuteOfDay = prefs[Keys.REMINDER1_MINUTE] ?: (20 * 60),
            reminder2Enabled = prefs[Keys.REMINDER2_ENABLED] ?: false,
            reminder2MinuteOfDay = prefs[Keys.REMINDER2_MINUTE] ?: (22 * 60),
            shareShowHabitName = prefs[Keys.SHARE_HABIT_NAME] ?: true,
            shareShowStreak = prefs[Keys.SHARE_STREAK] ?: true,
            shareShowCompletion = prefs[Keys.SHARE_COMPLETION] ?: true,
            shareShowTime = prefs[Keys.SHARE_TIME] ?: true,
            shareShowBranding = prefs[Keys.SHARE_BRANDING] ?: true,
            nightWatchDaysToEarn = prefs[Keys.NIGHT_WATCH_DAYS] ?: 14,
            nightWatchMaxStored = prefs[Keys.NIGHT_WATCH_MAX] ?: 2,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false
        )
    }

    suspend fun setOnboardingCompleted(value: Boolean) = context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = value }
    suspend fun setTheme(theme: AppTheme) = context.dataStore.edit { it[Keys.THEME] = theme.name }
    suspend fun setTrueBlackBackgrounds(value: Boolean) = context.dataStore.edit { it[Keys.TRUE_BLACK] = value }
    suspend fun setNotificationStyle(style: NotificationStyle) = context.dataStore.edit { it[Keys.NOTIFICATION_STYLE] = style.name }
    suspend fun setHapticsEnabled(value: Boolean) = context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = value }
    suspend fun setSoundEnabled(value: Boolean) = context.dataStore.edit { it[Keys.SOUND_ENABLED] = value }
    suspend fun setDayCutoffHour(hour: Int) = context.dataStore.edit { it[Keys.DAY_CUTOFF_HOUR] = hour }
    suspend fun setReminder1(enabled: Boolean, minuteOfDay: Int) = context.dataStore.edit {
        it[Keys.REMINDER1_ENABLED] = enabled
        it[Keys.REMINDER1_MINUTE] = minuteOfDay
    }
    suspend fun setReminder2(enabled: Boolean, minuteOfDay: Int) = context.dataStore.edit {
        it[Keys.REMINDER2_ENABLED] = enabled
        it[Keys.REMINDER2_MINUTE] = minuteOfDay
    }
    suspend fun setSharePrefs(name: Boolean, streak: Boolean, completion: Boolean, time: Boolean, branding: Boolean) =
        context.dataStore.edit {
            it[Keys.SHARE_HABIT_NAME] = name
            it[Keys.SHARE_STREAK] = streak
            it[Keys.SHARE_COMPLETION] = completion
            it[Keys.SHARE_TIME] = time
            it[Keys.SHARE_BRANDING] = branding
        }
    suspend fun setReducedMotion(value: Boolean) = context.dataStore.edit { it[Keys.REDUCED_MOTION] = value }
}
