package com.alexey.kozyakov.snake.effects.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.alexey.kozyakov.snake.model.SnakeGameEvent
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnakeGameHapticFeedbackPlayer(
    context: Context,
    coroutineScope: CoroutineScope,
    settingsRepository: SnakeGameSettingsRepository
) {
    private var hapticFeedbackEnabled = true

    private val systemVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager =
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
        manager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val hapticFeedbacks = mapOf(
        SnakeGameEvent.SNAKE_GROWS to HapticFeedback.SHORT,
        SnakeGameEvent.BAD_APPLE_EATEN to HapticFeedback.SHORT,
        SnakeGameEvent.BOMB_EATEN to HapticFeedback.MEDIUM,
        SnakeGameEvent.OMNIVOROUS_APPLE_EATEN to HapticFeedback.MEDIUM,
        SnakeGameEvent.GOLDEN_APPLE_EATEN to HapticFeedback.MEDIUM,
        SnakeGameEvent.GAME_OVER to HapticFeedback.LONG,
        SnakeGameEvent.COIN_PICKED to HapticFeedback.MEDIUM,
        SnakeGameEvent.DIAMOND_PICKED to HapticFeedback.LONG
    )

    init {
        coroutineScope.launch {
            settingsRepository.observe().collect { settings ->
                hapticFeedbackEnabled = settings.vibrationEnabled
            }
        }
    }

    fun playHapticFeedback(events: List<SnakeGameEvent>) {
        if (!hapticFeedbackEnabled) {
            return
        }
        for (event in events) {
            val hapticFeedback = hapticFeedbacks[event] ?: continue
            playHapticFeedback(hapticFeedback)
        }
    }

    private fun playHapticFeedback(hapticFeedback: HapticFeedback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect =
                VibrationEffect.createOneShot(hapticFeedback.duration, hapticFeedback.amplitude)
            systemVibrator.vibrate(effect)
        } else {
            systemVibrator.vibrate(hapticFeedback.duration)
        }
    }
}

private enum class HapticFeedback(val duration: Long, val amplitude: Int) {
    SHORT(1, 85),
    MEDIUM(3, 120),
    LONG(10, 160)
}
