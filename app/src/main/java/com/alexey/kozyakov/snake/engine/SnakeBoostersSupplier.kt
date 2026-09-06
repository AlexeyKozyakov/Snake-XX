package com.alexey.kozyakov.snake.engine

interface SnakeBoostersSupplier {
    fun consumeWallEatingBooster(): Boolean
    fun consumeSnakeEatingBooster(): Boolean

    companion object {
        fun empty(): SnakeBoostersSupplier = EmptySnakeBoostersSupplier
    }
}

private object EmptySnakeBoostersSupplier : SnakeBoostersSupplier {
    override fun consumeWallEatingBooster() = false
    override fun consumeSnakeEatingBooster() = false
}
