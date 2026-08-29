package com.alexey.kozyakov.snake.ui.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import com.alexey.kozyakov.snake.di.languageRepository
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguage
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguageRepository
import com.alexey.kozyakov.snake.ui.base.RetainedStateHolder
import com.alexey.kozyakov.snake.ui.base.asComposeState
import kotlinx.coroutines.flow.map

class SnakeGameLanguageState(
    private val languageRepository: SnakeGameLanguageRepository
) : RetainedStateHolder() {
    val languages by languageRepository
        .observe()
        .map { selectedLanguage ->
            SnakeGameLanguage.entries.map { language ->
                LanguageItem(
                    language = language,
                    selected = language == selectedLanguage
                )
            }
        }
        .asComposeState(initialValue = emptyList())

    fun setLanguage(language: SnakeGameLanguage) {
        languageRepository.save(language)
    }
}

@Composable
fun retainSnakeGameLanguageState(): SnakeGameLanguageState {
    return retain { SnakeGameLanguageState(languageRepository) }
}
