package com.alexey.kozyakov.snake.engine

interface SnakeBoostersSupplier {
    fun consumeWallEatingBooster(): Boolean
    fun consumeSnakeEatingBooster(): Boolean

    object Empty : SnakeBoostersSupplier {
        override fun consumeWallEatingBooster() = false
        override fun consumeSnakeEatingBooster() = false
    }
}
