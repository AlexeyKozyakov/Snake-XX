package com.alexey.kozyakov.snake.storage.language

import androidx.annotation.StringRes
import com.alexey.kozyakov.R

enum class SnakeGameLanguage(
    val code: String,
    @StringRes val nameResId: Int
) {
    SYSTEM(
        code = "",
        nameResId = R.string.language_system
    ),
    ENGLISH(
        code = "en",
        nameResId = R.string.language_english
    ),
    RUSSIAN(
        code = "ru",
        nameResId = R.string.language_russian
    )
}
