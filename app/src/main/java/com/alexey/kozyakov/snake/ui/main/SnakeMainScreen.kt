package com.alexey.kozyakov.snake.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.alexey.kozyakov.snake.ui.SnakeSplashScreen
import com.alexey.kozyakov.snake.ui.navigation.SnakeGameNavigationGraph

@Composable
fun SnakeMainScreen(modifier: Modifier = Modifier) {
    val state = retainSnakeMainScreenState()

    LifecycleResumeEffect(Unit) {
        state.resume()
        onPauseOrDispose {
            state.pause()
        }
    }

    if (state.loading) {
        SnakeSplashScreen(modifier)
    } else {
        SnakeGameNavigationGraph(modifier)
    }
}
