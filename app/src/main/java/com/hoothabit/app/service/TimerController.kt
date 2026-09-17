package com.hoothabit.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TimerUiState(
    val habitId: Long = -1,
    val targetSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
) {
    val isActive: Boolean get() = habitId != -1L
    val elapsedSeconds: Int get() = (targetSeconds - remainingSeconds).coerceAtLeast(0)
}

/**
 * Shared, process-wide timer state. [TimerService] owns the authoritative
 * countdown (survives backgrounding via a real foreground service); the UI
 * only observes this flow, so it stays correct even if the composable
 * hosting the timer screen is torn down and recreated.
 */
object TimerController {
    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state

    internal fun update(newState: TimerUiState) {
        _state.value = newState
    }

    internal fun reset() {
        _state.value = TimerUiState()
    }
}
