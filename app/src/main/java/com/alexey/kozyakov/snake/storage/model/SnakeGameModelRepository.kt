package com.alexey.kozyakov.snake.storage.model

import com.alexey.kozyakov.snake.model.SnakeGameModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SnakeGameModelRepository(
    private val fileSaver: SnakeGameModelFileSaver
) {
    private val gameModelFlow = MutableStateFlow<SnakeGameModel?>(null)

    suspend fun init() {
        gameModelFlow.value = fileSaver.load()
    }

    suspend fun save(model: SnakeGameModel) {
        gameModelFlow.value = model
        fileSaver.save(model)
    }

    suspend fun reset() {
        gameModelFlow.value = null
        fileSaver.reset()
    }

    fun observe(): StateFlow<SnakeGameModel?> {
        return gameModelFlow
    }
}
