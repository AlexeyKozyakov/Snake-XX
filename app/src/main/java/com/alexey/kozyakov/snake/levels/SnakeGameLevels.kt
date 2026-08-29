package com.alexey.kozyakov.snake.levels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.model.Position
import com.alexey.kozyakov.snake.model.SnakeGameModel
import com.alexey.kozyakov.snake.model.SnakeModel
import com.alexey.kozyakov.snake.model.SnakeType
import com.alexey.kozyakov.snake.model.Wall
import com.alexey.kozyakov.snake.storage.skins.SnakeSkin
import com.alexey.kozyakov.snake.ui.game.rememberSnakeGameRenderer

private val levels = levels {
    level { width, height ->
        horizontal(y = 0, x1 = 3, x2 = width - 4)
        horizontal(y = height - 1, x1 = 3, x2 = width - 4)
        vertical(x = 0, y1 = 3, y2 = height - 4)
        vertical(x = width - 1, y1 = 3, y2 = height - 4)
    }
    level { width, height ->
        horizontal(y = height / 2 - 2, x1 = width / 2 - 2, x2 = width / 2 + 2)
        vertical(x = 2, y1 = 5, y2 = height / 2 - 5)
        vertical(x = 2, y1 = height / 2 + 5, y2 = height - 5)
        vertical(x = width - 2, y1 = 5, y2 = height / 2 - 5)
        vertical(x = width - 2, y1 = height / 2 + 5, y2 = height - 5)
    }
    level { width, height ->
        block(x = width / 2, y = height / 2 - 2)
        block(x = width / 2 - 3, y = height / 2 - 2)
        block(x = width / 2 - 6, y = height / 2 - 2)
        block(x = width / 2 + 3, y = height / 2 - 2)
        block(x = width / 2 + 6, y = height / 2 - 2)
        vertical(x = width / 2, y1 = 3, y2 = height / 2 - 6)
        vertical(x = width / 2, y1 = height / 2 + 2, y2 = height - 4)
    }
    level { width, height ->
        block(x = width / 4, y = height / 4)
        block(x = width / 4, y = height * 3 / 4)
        block(x = width * 3 / 4, y = height / 4)
        block(x = width * 3 / 4, y = height * 3 / 4)
        horizontal(y = 3, x1 = width / 2 - 4, x2 = width / 2 + 4)
        horizontal(y = height - 4, x1 = width / 2 - 4, x2 = width / 2 + 4)
        horizontal(y = height / 2 - 1, x1 = 1, x2 = 3)
        horizontal(y = height / 2 - 1, x1 = width - 4, x2 = width - 2)
    }
    level { width, height ->
        vertical(x = 2, y1 = 1, y2 = 4)
        vertical(x = width - 2, y1 = 1, y2 = 4)
        vertical(x = 2, y1 = height - 4, y2 = height - 2)
        vertical(x = width - 2, y1 = height - 4, y2 = height - 2)
        horizontal(y = 5, x1 = 5, x2 = width - 5)
        horizontal(y = height - 5, x1 = 5, x2 = width - 5)
        vertical(x = 3, y1 = 10, y2 = height - 10)
        vertical(x = width - 3, y1 = 10, y2 = height - 10)
    }
    level { width, height ->
        horizontal(y = 5, x1 = width / 2 - 3, x2 = width / 2 + 3)
        vertical(x = width / 2, y1 = 2, y2 = 8)
        horizontal(y = height - 5, x1 = width / 2 - 3, x2 = width / 2 + 3)
        vertical(x = width / 2, y1 = height - 8, y2 = height - 2)
        horizontal(y = height / 2 - 2, x1 = 1, x2 = 5)
        vertical(x = 3, y1 = height / 2 - 4, y2 = height / 2)
        horizontal(y = height / 2 - 2, x1 = width - 6, x2 = width - 2)
        block(x = 3, y = height * 3 / 4)
        block(x = width - 4, y = height * 3 / 4)
        block(x = 3, y = height / 4)
        block(x = width - 4, y = height / 4)
    }
    level { width, height ->
        horizontal(y = 2, x1 = 2, x2 = width - 3)
        horizontal(y = 5, x1 = 2, x2 = width - 3)
        horizontal(y = 8, x1 = 2, x2 = width - 3)
        vertical(x = 2, y1 = height / 2 + 2, y2 = height - 3)
        vertical(x = 5, y1 = height / 2 + 2, y2 = height - 3)
        vertical(x = width - 3, y1 = height / 2 + 2, y2 = height - 3)
        vertical(x = width - 6, y1 = height / 2 + 2, y2 = height - 3)
        block(x = 2, y = height / 2 - 2)
        block(x = width - 3, y = height / 2 - 2)
        block(x = width / 2, y = height / 2 - 2)
    }
}

fun levelWalls(level: Int, gridWidth: Int, gridHeight: Int): List<Wall> {
    if (gridWidth > gridHeight) {
        return levelWalls(
            level = level,
            gridWidth = gridHeight,
            gridHeight = gridWidth
        ).map { wall -> wall.transposed() }
    }
    return levels[level % levels.size](gridWidth, gridHeight)
}

private class PreviewLevelProvider : PreviewParameterProvider<Int> {
    override val values: Sequence<Int>
        get() = levels.indices.asSequence()
}

@Composable
@Preview
private fun Preview(
    @PreviewParameter(provider = PreviewLevelProvider::class) level: Int
) {
    val renderer = rememberSnakeGameRenderer(SnakeSkin.DEFAULT)
    val gridWidth = 15
    val gridHeight = 30
    Canvas(Modifier.fillMaxSize()) {
        renderer.renderSnakeGame(
            model = SnakeGameModel(
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                apples = sequenceOf(),
                snakes = listOf(
                    SnakeModel(
                        elements = sequenceOf(
                            Position(
                                x = gridWidth / 2 - 2,
                                y = gridHeight / 2
                            ),
                            Position(
                                x = gridWidth / 2 - 1,
                                y = gridHeight / 2
                            ),
                            Position(
                                x = gridWidth / 2,
                                y = gridHeight / 2
                            )
                        ),
                        omnivorousTicksRemaining = 0,
                        type = SnakeType.MAIN,
                        direction = Direction.RIGHT,
                        length = 3
                    ),
                    SnakeModel(
                        elements = sequenceOf(
                            Position(
                                x = gridWidth / 2 - 2,
                                y = gridHeight / 2 - 4
                            ),
                            Position(
                                x = gridWidth / 2 - 1,
                                y = gridHeight / 2 - 4
                            ),
                            Position(
                                x = gridWidth / 2,
                                y = gridHeight / 2 - 4
                            )
                        ),
                        omnivorousTicksRemaining = 0,
                        type = SnakeType.SECONDARY,
                        direction = Direction.RIGHT,
                        length = 3
                    )
                ),
                walls = levelWalls(
                    level = level,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight
                ),
                gameIsOver = false,
                level = 0,
                remainingLengthToGainLevel = 0,
                appleCount = 0,
                score = 0
            )
        )
    }
}
