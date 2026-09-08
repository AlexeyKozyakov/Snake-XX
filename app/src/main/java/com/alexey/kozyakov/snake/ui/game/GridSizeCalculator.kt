package com.alexey.kozyakov.snake.ui.game

import com.alexey.kozyakov.snake.config.MAIN_GRID_DIMENSION
import kotlin.math.min

fun calculateGridDimensions(widthPx: Int, heightPx: Int): Pair<Int, Int> {
    val cellSize = min(widthPx, heightPx).toFloat() / MAIN_GRID_DIMENSION
    return (widthPx / cellSize).toInt() to (heightPx / cellSize).toInt()
}
