package com.alexey.kozyakov.snake.levels

import com.alexey.kozyakov.snake.model.Position
import com.alexey.kozyakov.snake.model.Wall


fun levels(
    builder: MutableList<(width: Int, height: Int) -> List<Wall>>.() -> Unit
): List<(width: Int, height: Int) -> List<Wall>> {
    return buildList {
        builder()
    }
}

fun MutableList<(width: Int, height: Int) -> List<Wall>>.level(
    builder: MutableList<Wall>.(width: Int, height: Int) -> Unit
) {
    add { width, height ->
        buildList {
            builder(width, height)
        }
    }
}

fun MutableList<Wall>.block(x: Int, y: Int) {
    add(Wall.SingleBlock(position = Position(x, y)))
}

fun MutableList<Wall>.horizontal(y: Int, x1: Int, x2: Int) {
    add(
        Wall.HorizontalLine(
            startPosition = Position(x = x1, y = y),
            endPosition = Position(x = x2, y = y)
        )
    )
}

fun MutableList<Wall>.vertical(x: Int, y1: Int, y2: Int) {
    add(
        Wall.VerticalLine(
            startPosition = Position(x = x, y = y1),
            endPosition = Position(x = x, y = y2)
        )
    )
}
