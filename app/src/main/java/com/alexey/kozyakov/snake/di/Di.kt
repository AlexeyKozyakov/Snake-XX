package com.alexey.kozyakov.snake.di

import com.alexey.kozyakov.snake.storage.shop.PurchaseRepository
import com.alexey.kozyakov.snake.storage.SnakeGameBalanceRepository
import com.alexey.kozyakov.snake.storage.SnakeGameHighScoreRepository
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguageRepository
import com.alexey.kozyakov.snake.storage.model.SnakeGameModelFileSaver
import com.alexey.kozyakov.snake.storage.model.SnakeGameModelRepository
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettingsRepository
import com.alexey.kozyakov.snake.storage.skins.SnakeSkinRepository
import com.alexey.kozyakov.snake.storage.snakeGamePreferences
import com.alexey.kozyakov.snake.storage.upgrade.SnakeUpgradeRepository
import com.alexey.kozyakov.snake.ui.SnakeGameApplication

val context by lazy {
    SnakeGameApplication.instance.applicationContext!!
}

val gamePreferences by lazy {
    context.snakeGamePreferences
}

val highScoreRepository by lazy {
    SnakeGameHighScoreRepository(gamePreferences)
}

val gameModelFileSaver by lazy {
    SnakeGameModelFileSaver(context)
}

val gameModelRepository by lazy {
    SnakeGameModelRepository(gameModelFileSaver)
}

val gameSettingsRepository by lazy {
    SnakeGameSettingsRepository(gamePreferences)
}

val languageRepository by lazy {
    SnakeGameLanguageRepository()
}

val snakeSkinRepository by lazy {
    SnakeSkinRepository(gamePreferences)
}

val balanceRepository by lazy {
    SnakeGameBalanceRepository(gamePreferences)
}

val purchaseRepository by lazy {
    PurchaseRepository(gamePreferences)
}

val upgradeRepository by lazy {
    SnakeUpgradeRepository(purchaseRepository)
}
