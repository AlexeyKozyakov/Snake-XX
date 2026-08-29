package com.alexey.kozyakov.snake.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import com.alexey.kozyakov.snake.di.context
import com.alexey.kozyakov.snake.di.gameSettingsRepository
import com.alexey.kozyakov.snake.di.languageRepository
import com.alexey.kozyakov.snake.di.snakeSkinRepository
import com.alexey.kozyakov.snake.effects.haptic.SnakeGameHapticFeedbackPlayer
import com.alexey.kozyakov.snake.effects.sound.SnakeGameSoundEffectsPlayer
import com.alexey.kozyakov.snake.model.SnakeGameEvent
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguage
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguageRepository
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettings
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettingsRepository
import com.alexey.kozyakov.snake.storage.skins.SnakeSkin
import com.alexey.kozyakov.snake.storage.skins.SnakeSkinRepository
import com.alexey.kozyakov.snake.ui.base.RetainedStateHolder
import com.alexey.kozyakov.snake.ui.base.asComposeState
import kotlinx.coroutines.launch

class SnakeGameSettingsState(
    private val settingsRepository: SnakeGameSettingsRepository,
    languageRepository: SnakeGameLanguageRepository,
    skinRepository: SnakeSkinRepository,
    context: Context
) : RetainedStateHolder() {

    val settings by settingsRepository
        .observe()
        .asComposeState(initialValue = SnakeGameSettings())

    val language by languageRepository
        .observe()
        .asComposeState(initialValue = SnakeGameLanguage.SYSTEM)

    val skin by skinRepository
        .observe()
        .asComposeState(initialValue = SnakeSkin.DEFAULT)

    private val hapticFeedbackPlayer =
        SnakeGameHapticFeedbackPlayer(context, stateHolderScope, settingsRepository)
    private val soundEffectsPlayer =
        SnakeGameSoundEffectsPlayer(context, stateHolderScope, settingsRepository)

    fun toggleVibration() {
        val newSettings = settings.copy(vibrationEnabled = !settings.vibrationEnabled)
        stateHolderScope.launch {
            settingsRepository.save(newSettings)
            if (newSettings.vibrationEnabled) {
                hapticFeedbackPlayer.playHapticFeedback(listOf(SnakeGameEvent.GAME_OVER))
            }
        }
    }

    fun toggleMusic() {
        val newSettings = settings.copy(musicEnabled = !settings.musicEnabled)
        stateHolderScope.launch {
            settingsRepository.save(newSettings)
        }
    }

    fun toggleSoundEffects() {
        val newSettings = settings.copy(soundEffectsEnabled = !settings.soundEffectsEnabled)
        stateHolderScope.launch {
            settingsRepository.save(newSettings)
            if (newSettings.soundEffectsEnabled) {
                soundEffectsPlayer.playSoundEffects(listOf(SnakeGameEvent.GOLDEN_APPLE_EATEN))
            }
        }
    }

    override fun dispose() {
        super.dispose()
        soundEffectsPlayer.dispose()
    }
}

@Composable
fun retainSnakeGameSettingsState(): SnakeGameSettingsState {
    return retain {
        SnakeGameSettingsState(
            gameSettingsRepository,
            languageRepository,
            snakeSkinRepository,
            context
        )
    }
}
