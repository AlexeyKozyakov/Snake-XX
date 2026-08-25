package com.alexey.kozyakov.snake.storage.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SnakeGameLanguageRepository {
    private val languageFlow = MutableStateFlow(getAppLanguage())

    fun save(language: SnakeGameLanguage) {
        AppCompatDelegate.setApplicationLocales(
            when (language) {
                SnakeGameLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                else -> LocaleListCompat.forLanguageTags(language.code)
            }
        )
        languageFlow.value = language
    }

    fun observe(): Flow<SnakeGameLanguage> {
        return languageFlow
    }

    private fun getAppLanguage(): SnakeGameLanguage {
        val code = AppCompatDelegate.getApplicationLocales()[0]?.language ?: ""
        return SnakeGameLanguage.entries.first { language -> language.code == code }
    }
}
