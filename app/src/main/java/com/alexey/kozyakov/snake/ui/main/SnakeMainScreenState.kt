package com.alexey.kozyakov.snake.ui.main

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import com.alexey.kozyakov.snake.di.context
import com.alexey.kozyakov.snake.di.gameModelRepository
import com.alexey.kozyakov.snake.di.gameSettingsRepository
import com.alexey.kozyakov.snake.music.SnakeGameBackgroundMusicPlayer
import com.alexey.kozyakov.snake.storage.model.SnakeGameModelRepository
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettingsRepository
import com.alexey.kozyakov.snake.ui.base.RetainedStateHolder
import kotlinx.coroutines.launch

class SnakeMainScreenState(
    gameModelRepository: SnakeGameModelRepository,
    settingsRepository: SnakeGameSettingsRepository,
    context: Context
) : RetainedStateHolder() {
    init {
        stateHolderScope.launch {
            gameModelRepository.init()
            loading = false
        }
    }

    private val musicPlayer =
        SnakeGameBackgroundMusicPlayer(context, stateHolderScope, settingsRepository)

    var loading by mutableStateOf(true)
        private set

    fun pause() {
        musicPlayer.pause()
    }

    fun resume() {
        musicPlayer.play()
    }

    override fun dispose() {
        super.dispose()
        musicPlayer.dispose()
    }
}

@Composable
fun retainSnakeMainScreenState(): SnakeMainScreenState {
    return retain { SnakeMainScreenState(gameModelRepository, gameSettingsRepository, context) }
}
