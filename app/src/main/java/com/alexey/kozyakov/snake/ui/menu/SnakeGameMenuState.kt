package com.alexey.kozyakov.snake.ui.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import com.alexey.kozyakov.snake.di.gameModelRepository
import com.alexey.kozyakov.snake.storage.model.SnakeGameModelRepository
import com.alexey.kozyakov.snake.ui.base.RetainedStateHolder
import com.alexey.kozyakov.snake.ui.base.asComposeState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SnakeGameMenuScreenState(
    private val gameModelRepository: SnakeGameModelRepository,
) : RetainedStateHolder() {
    val canContinueGame by gameModelRepository
        .observe()
        .map { model -> model != null }
        .asComposeState(initialValue = false)

    val level by gameModelRepository
        .observe()
        .map { model -> (model?.level ?: 0) + 1 }
        .asComposeState(initialValue = 1)

    fun resetGame() {
        stateHolderScope.launch {
            gameModelRepository.reset()
        }
    }
}

@Composable
fun retainSnakeGameMenuState(): SnakeGameMenuScreenState {
    return retain { SnakeGameMenuScreenState(gameModelRepository) }
}
