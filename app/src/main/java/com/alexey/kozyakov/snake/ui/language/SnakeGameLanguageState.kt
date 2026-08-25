package com.alexey.kozyakov.snake.ui.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import com.alexey.kozyakov.snake.di.languageRepository
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguage
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguageRepository
import com.alexey.kozyakov.snake.ui.base.RetainedStateHolder
import com.alexey.kozyakov.snake.ui.base.asComposeState

class SnakeGameLanguageState(
    private val languageRepository: SnakeGameLanguageRepository
) : RetainedStateHolder() {
    val language by languageRepository
        .observe()
        .asComposeState(stateHolderScope, initialValue = SnakeGameLanguage.SYSTEM)

    fun setLanguage(language: SnakeGameLanguage) {
        languageRepository.save(language)
    }
}

@Composable
fun retainSnakeGameLanguageState(): SnakeGameLanguageState {
    return retain { SnakeGameLanguageState(languageRepository) }
}
