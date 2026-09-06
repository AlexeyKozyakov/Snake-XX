package com.alexey.kozyakov.snake.ui.game

import com.alexey.kozyakov.snake.config.MIN_MAIN_GRID_DIMENSION
import kotlin.math.max

fun calculateGridDimensions(widthPx: Int, heightPx: Int): Pair<Int, Int> {
    val gcd = widthPx.toBigInteger().gcd(heightPx.toBigInteger()).toInt()
    val mainDimensionSizePx = max(widthPx, heightPx)
    var cellSize = gcd
    while (mainDimensionSizePx / cellSize < MIN_MAIN_GRID_DIMENSION && cellSize % 2 == 0) {
        cellSize /= 2
    }
    val gridWidth = widthPx / cellSize
    val gridHeight = heightPx / cellSize
    return gridWidth to gridHeight
}
