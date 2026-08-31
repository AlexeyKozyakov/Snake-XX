package com.alexey.kozyakov.snake.ui.main

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import com.alexey.kozyakov.snake.di.context
import com.alexey.kozyakov.snake.di.gameModelRepository
import com.alexey.kozyakov.snake.di.gamePreferences
import com.alexey.kozyakov.snake.di.gameSettingsRepository
import com.alexey.kozyakov.snake.music.SnakeGameBackgroundMusicPlayer
import com.alexey.kozyakov.snake.storage.base.CachedDataStore
import com.alexey.kozyakov.snake.storage.model.SnakeGameModelRepository
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettingsRepository
import com.alexey.kozyakov.snake.ui.base.RetainedStateHolder
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class SnakeMainScreenState(
    gameModelRepository: SnakeGameModelRepository,
    settingsRepository: SnakeGameSettingsRepository,
    gamePreferences: CachedDataStore<Preferences>,
    context: Context
) : RetainedStateHolder() {
    init {
        stateHolderScope.launch {
            val modelInitJob = launch { gameModelRepository.init() }
            val preferencesInitJob = launch { gamePreferences.init() }
            joinAll(modelInitJob, preferencesInitJob)
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
    return retain {
        SnakeMainScreenState(
            gameModelRepository,
            gameSettingsRepository,
            gamePreferences,
            context
        )
    }
}
