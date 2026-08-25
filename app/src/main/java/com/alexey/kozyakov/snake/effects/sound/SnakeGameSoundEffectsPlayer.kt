package com.alexey.kozyakov.snake.effects.sound

import android.content.Context
import android.media.SoundPool
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.model.SnakeGameEvent
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnakeGameSoundEffectsPlayer(
    private val context: Context,
    coroutineScope: CoroutineScope,
    settingsRepository: SnakeGameSettingsRepository
) {
    private var soundEffectsEnabled = true
    private val soundPool = SoundPool
        .Builder()
        .setMaxStreams(10)
        .build()

    private val eventSounds = mapOf(
        SnakeGameEvent.GOLDEN_APPLE_EATEN to loadSound(R.raw.sound_golden_apple_eaten),
        SnakeGameEvent.SNAKE_GROWS to loadSound(R.raw.sound_snake_grows),
        SnakeGameEvent.BAD_APPLE_EATEN to loadSound(R.raw.sound_bad_apple_eaten),
        SnakeGameEvent.OMNIVOROUS_APPLE_EATEN to loadSound(R.raw.sound_omnivorous_apple_eaten),
        SnakeGameEvent.BOMB_EATEN to loadSound(R.raw.sound_bomb_eaten),
        SnakeGameEvent.LEVEL_GAINED to loadSound(R.raw.sound_level_gained),
        SnakeGameEvent.GAME_OVER to loadSound(R.raw.sound_game_over)
    )

    init {
        coroutineScope.launch {
            settingsRepository.observe().collect { settings ->
                soundEffectsEnabled = settings.soundEffectsEnabled
            }
        }
    }

    fun playSoundEffects(events: List<SnakeGameEvent>) {
        if (!soundEffectsEnabled) {
            return
        }
        for (event in events) {
            val soundId = eventSounds[event] ?: continue
            playSound(soundId)
        }
    }

    fun dispose() {
        soundPool.release()
    }

    private fun loadSound(resId: Int): Int {
        return soundPool.load(context, resId, 1)
    }

    private fun playSound(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }
}
