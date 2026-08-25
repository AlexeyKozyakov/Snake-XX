package com.alexey.kozyakov.snake.music

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SnakeGameBackgroundMusicPlayer(
    context: Context,
    coroutineScope: CoroutineScope,
    settingsRepository: SnakeGameSettingsRepository
) {
    private val paused = MutableStateFlow(true)
    private val exoPlayer = ExoPlayer.Builder(context).build()

    init {
        preparePlayer()
        val shouldPlayFlow = combine(
            paused, settingsRepository.observe()
        ) { paused, settings ->
            !paused && settings.musicEnabled
        }
        coroutineScope.launch {
            shouldPlayFlow.collect { shouldPlay ->
                if (shouldPlay) {
                    exoPlayer.play()
                } else {
                    exoPlayer.pause()
                }
            }
        }
    }

    fun play() {
        paused.value = false
    }

    fun pause() {
        paused.value = true
    }

    fun dispose() {
        exoPlayer.release()
    }

    private fun preparePlayer() {
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .path(R.raw.music_black_and_yellow.toString())
            .build()
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.volume = 0.6f
        exoPlayer.prepare()
    }
}
