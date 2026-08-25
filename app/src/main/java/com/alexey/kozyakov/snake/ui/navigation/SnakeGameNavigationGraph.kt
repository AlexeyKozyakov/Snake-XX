package com.alexey.kozyakov.snake.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alexey.kozyakov.snake.ui.game.SnakeGameScreen
import com.alexey.kozyakov.snake.ui.language.SnakeGameLanguageScreen
import com.alexey.kozyakov.snake.ui.menu.SnakeGameMenuScreen
import com.alexey.kozyakov.snake.ui.settings.SnakeGameSettingsScreen

@Composable
fun SnakeGameNavigationGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SnakeGameDestination.MenuScreen
    ) {
        composable<SnakeGameDestination.MenuScreen> {
            SnakeGameMenuScreen(
                modifier,
                navigateToGameScreen = {
                    navController.navigate(SnakeGameDestination.GameScreen)
                },
                navigateToSettingsScreen = {
                    navController.navigate(SnakeGameDestination.SettingsScreen)
                }
            )
        }

        composable<SnakeGameDestination.GameScreen> {
            SnakeGameScreen(modifier)
        }

        composable<SnakeGameDestination.SettingsScreen> {
            SnakeGameSettingsScreen(
                modifier,
                navigateBack = navController::popBackStack,
                navigateToLanguageSettings = {
                    navController.navigate(SnakeGameDestination.LanguageScreen)
                }
            )
        }

        composable<SnakeGameDestination.LanguageScreen> {
            SnakeGameLanguageScreen(
                modifier,
                navigateBack = navController::popBackStack
            )
        }
    }
}
