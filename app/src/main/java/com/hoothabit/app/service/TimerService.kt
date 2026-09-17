package com.hoothabit.app.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import com.hoothabit.app.HootHabitApp
import com.hoothabit.app.R
import com.hoothabit.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A real foreground service driving the habit timer, so it keeps counting
 * whether the screen is locked, the app is backgrounded, or the activity is
 * destroyed and recreated (spec #9: "extremely reliable").
 */
class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob())
    private var tickJob: Job? = null
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1)
                val targetSeconds = intent.getIntExtra(EXTRA_TARGET_SECONDS, 0)
                startSession(habitId, targetSeconds)
            }
            ACTION_PAUSE -> pause()
            ACTION_RESUME -> resume()
            ACTION_STOP -> stopSession()
            else -> restoreIfNeeded()
        }
        return START_STICKY
    }

    private fun startSession(habitId: Long, targetSeconds: Int) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_HABIT_ID, habitId)
            .putInt(KEY_TARGET_SECONDS, targetSeconds)
            .putLong(KEY_START_TIME, now)
            .putLong(KEY_PAUSED_AT, -1L)
            .putLong(KEY_ACCUMULATED_PAUSE, 0L)
            .putBoolean(KEY_RUNNING, true)
            .apply()
        startForeground(NOTIFICATION_ID, buildNotification(targetSeconds, false))
        beginTicking()
    }

    private fun pause() {
        if (!prefs.getBoolean(KEY_RUNNING, false)) return
        prefs.edit().putLong(KEY_PAUSED_AT, System.currentTimeMillis()).apply()
        tickJob?.cancel()
        val remaining = computeRemainingSeconds()
        TimerController.update(currentUiState(remaining, isRunning = false, isPaused = true))
        updateNotification(remaining, isPaused = true)
    }

    private fun resume() {
        val pausedAt = prefs.getLong(KEY_PAUSED_AT, -1L)
        if (pausedAt > 0) {
            val pauseDuration = System.currentTimeMillis() - pausedAt
            val accumulated = prefs.getLong(KEY_ACCUMULATED_PAUSE, 0L) + pauseDuration
            prefs.edit()
                .putLong(KEY_ACCUMULATED_PAUSE, accumulated)
                .putLong(KEY_PAUSED_AT, -1L)
                .apply()
        }
        beginTicking()
    }

    private fun stopSession() {
        tickJob?.cancel()
        prefs.edit().clear().apply()
        TimerController.reset()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun restoreIfNeeded() {
        if (!prefs.getBoolean(KEY_RUNNING, false)) return
        val remaining = computeRemainingSeconds()
        if (remaining <= 0) {
            stopSession()
            return
        }
        val isPaused = prefs.getLong(KEY_PAUSED_AT, -1L) > 0
        startForeground(NOTIFICATION_ID, buildNotification(prefs.getInt(KEY_TARGET_SECONDS, 0), isPaused))
        if (!isPaused) beginTicking() else TimerController.update(currentUiState(remaining, isRunning = false, isPaused = true))
    }

    private fun beginTicking() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive) {
                val remaining = computeRemainingSeconds()
                TimerController.update(currentUiState(remaining, isRunning = remaining > 0, isPaused = false))
                if (remaining <= 0) {
                    updateNotification(0, isPaused = false, finished = true)
                    break
                }
                updateNotification(remaining, isPaused = false)
                delay(1000)
            }
        }
    }

    private fun computeRemainingSeconds(): Int {
        val target = prefs.getInt(KEY_TARGET_SECONDS, 0)
        val start = prefs.getLong(KEY_START_TIME, System.currentTimeMillis())
        val pausedAt = prefs.getLong(KEY_PAUSED_AT, -1L)
        val accumulatedPause = prefs.getLong(KEY_ACCUMULATED_PAUSE, 0L)
        val now = if (pausedAt > 0) pausedAt else System.currentTimeMillis()
        val elapsedMillis = (now - start - accumulatedPause).coerceAtLeast(0)
        val elapsedSeconds = (elapsedMillis / 1000).toInt()
        return (target - elapsedSeconds).coerceAtLeast(0)
    }

    private fun currentUiState(remaining: Int, isRunning: Boolean, isPaused: Boolean) = TimerUiState(
        habitId = prefs.getLong(KEY_HABIT_ID, -1),
        targetSeconds = prefs.getInt(KEY_TARGET_SECONDS, 0),
        remainingSeconds = remaining,
        isRunning = isRunning,
        isPaused = isPaused
    )

    private fun buildNotification(targetSeconds: Int, isPaused: Boolean) =
        buildNotificationInternal(targetSeconds, isPaused)

    private fun updateNotification(remaining: Int, isPaused: Boolean, finished: Boolean = false) {
        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotificationInternal(remaining, isPaused, finished))
    }

    private fun buildNotificationInternal(remainingSeconds: Int, isPaused: Boolean, finished: Boolean = false): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val text = when {
            finished -> "Time's up"
            isPaused -> "Paused · %02d:%02d remaining".format(minutes, seconds)
            else -> "%02d:%02d remaining".format(minutes, seconds)
        }
        return NotificationCompat.Builder(this, HootHabitApp.CHANNEL_TIMER)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(!finished)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        tickJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        private const val PREFS_NAME = "hoot_timer_state"
        private const val KEY_HABIT_ID = "habit_id"
        private const val KEY_TARGET_SECONDS = "target_seconds"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_PAUSED_AT = "paused_at"
        private const val KEY_ACCUMULATED_PAUSE = "accumulated_pause"
        private const val KEY_RUNNING = "running"
        private const val NOTIFICATION_ID = 42

        const val ACTION_START = "com.hoothabit.app.action.START"
        const val ACTION_PAUSE = "com.hoothabit.app.action.PAUSE"
        const val ACTION_RESUME = "com.hoothabit.app.action.RESUME"
        const val ACTION_STOP = "com.hoothabit.app.action.STOP"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_TARGET_SECONDS = "extra_target_seconds"

        fun start(context: Context, habitId: Long, targetSeconds: Int) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(EXTRA_TARGET_SECONDS, targetSeconds)
            }
            context.startForegroundService(intent)
        }

        fun pause(context: Context) {
            context.startService(Intent(context, TimerService::class.java).apply { action = ACTION_PAUSE })
        }

        fun resume(context: Context) {
            context.startForegroundService(Intent(context, TimerService::class.java).apply { action = ACTION_RESUME })
        }

        fun stop(context: Context) {
            context.startService(Intent(context, TimerService::class.java).apply { action = ACTION_STOP })
        }

        /** Call on app start to reconnect the UI to a session that survived a process restart. */
        fun reconnect(context: Context) {
            context.startService(Intent(context, TimerService::class.java))
        }
    }
}
