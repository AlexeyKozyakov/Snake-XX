package com.alexey.kozyakov.snake.ui.navigation

import kotlinx.serialization.Serializable

sealed interface SnakeGameDestination {
    @Serializable
    data object MenuScreen

    @Serializable
    data object GameScreen

    @Serializable
    data object SettingsScreen

    @Serializable
    data object LanguageScreen

    @Serializable
    data object ShopScreen
}
