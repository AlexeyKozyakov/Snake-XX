package com.alexey.kozyakov.snake.ui.game

import com.alexey.kozyakov.snake.storage.boosters.SnakeBooster

data class ConsumedBooster(
    val booster: SnakeBooster,
    val remainingCount: Int
) {
    companion object {
        fun default() = ConsumedBooster(SnakeBooster.WALLS_EATING, 0)
    }
}
