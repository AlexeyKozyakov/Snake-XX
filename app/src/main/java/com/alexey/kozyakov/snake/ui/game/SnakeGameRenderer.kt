package com.alexey.kozyakov.snake.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.model.Apple
import com.alexey.kozyakov.snake.model.AppleType
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.model.Position
import com.alexey.kozyakov.snake.model.SnakeGameModel
import com.alexey.kozyakov.snake.model.SnakeModel
import com.alexey.kozyakov.snake.model.SnakeType
import com.alexey.kozyakov.snake.model.Wall
import com.alexey.kozyakov.snake.storage.skins.SnakeSkin


class SnakeGameSprites(
    val redApple: ImageBitmap,
    val greenApple: ImageBitmap,
    val goldApple: ImageBitmap,
    val badApple: ImageBitmap,
    val coin: ImageBitmap,
    val diamond: ImageBitmap,
    val mainSnakeBody: ImageBitmap,
    val mainSnakeHead: ImageBitmap,
    val mainSnakeHeadXX: ImageBitmap,
    val secondarySnakeBody: ImageBitmap,
    val secondarySnakeHead: ImageBitmap,
    val secondarySnakeHeadXX: ImageBitmap,
    val easterEgg: ImageBitmap,
    val bomb: ImageBitmap,
    val blockWall: ImageBitmap,
    val leftWall: ImageBitmap,
    val horizontalWall: ImageBitmap,
    val rightWall: ImageBitmap,
    val bottomWall: ImageBitmap,
    val verticalWall: ImageBitmap,
    val topWall: ImageBitmap
)

class SnakeGameRenderer(private val sprites: SnakeGameSprites) {
    private val grassColor = Color(0xFF204821)
    private val gridColor = Color(0xFF547C54)
    private val goldColor = Color(0x72FFD700)

    context(scope: DrawScope)
    fun renderSnakeGame(model: SnakeGameModel) = with(scope) {
        val cellSize = size.width / model.gridWidth
        drawRect(color = grassColor, topLeft = Offset(0f, 0f), size = size)
        renderGrid(model.gridWidth, model.gridHeight, cellSize)
        renderWalls(model.walls, cellSize)
        model.snakes.reversed().forEach { snake ->
            if (!model.gameIsOver || snake.type != SnakeType.MAIN) {
                renderSnake(snake, cellSize)
            }
        }
        renderApples(model.apples, cellSize)
    }

    private fun DrawScope.renderGrid(width: Int, height: Int, cellSize: Float) {
        for (i in 0..width) {
            drawLine(
                color = gridColor,
                start = Offset(x = i * cellSize, y = 0f),
                end = Offset(x = i * cellSize, y = size.height)
            )
        }
        for (i in 0..height) {
            drawLine(
                color = gridColor,
                start = Offset(x = 0f, y = i * cellSize),
                end = Offset(x = size.width, y = i * cellSize)
            )
        }
    }

    private fun DrawScope.renderSnake(snake: SnakeModel, cellSize: Float) {
        val headSprite = when (snake.type) {
            SnakeType.MAIN ->
                if (snake.omnivorousTicksRemaining > 0) sprites.mainSnakeHeadXX else sprites.mainSnakeHead

            SnakeType.SECONDARY ->
                if (snake.omnivorousTicksRemaining > 0) sprites.secondarySnakeHeadXX else sprites.secondarySnakeHead
        }
        val bodySprite = when (snake.type) {
            SnakeType.MAIN -> sprites.mainSnakeBody
            SnakeType.SECONDARY -> sprites.secondarySnakeBody
        }
        val iterator = snake.elements.iterator()
        while (iterator.hasNext()) {
            val position = iterator.next()
            val sprite = if (iterator.hasNext()) bodySprite else headSprite
            renderScaledSprite(position, cellSize, sprite, scale = 1.35f)
        }
    }

    private fun DrawScope.renderApples(apples: Sequence<Apple>, cellSize: Float) {
        for (apple in apples) {
            val sprite = when (apple.type) {
                AppleType.GOOD_1 -> sprites.redApple
                AppleType.GOOD_2 -> sprites.greenApple
                AppleType.BAD -> sprites.badApple
                AppleType.OMNIVOROUSNESS -> sprites.easterEgg
                AppleType.BOMB -> sprites.bomb
                AppleType.GOLDEN -> sprites.goldApple
                AppleType.COIN -> sprites.coin
                AppleType.DIAMOND -> sprites.diamond
            }
            if (apple.type.isBonus) {
                drawCircle(
                    color = goldColor,
                    radius = cellSize,
                    center = Offset(
                        x = apple.position.x * cellSize + cellSize / 2,
                        y = apple.position.y * cellSize + cellSize / 2
                    )
                )
            }
            renderScaledSprite(apple.position, cellSize, sprite, scale = 1.35f)
        }
    }

    private fun DrawScope.renderWalls(walls: List<Wall>, cellSize: Float) {
        for (wall in walls) {
            when (wall) {
                is Wall.SingleBlock -> {
                    renderScaledSprite(
                        position = wall.position,
                        cellSize = cellSize,
                        sprite = sprites.blockWall,
                        scale = 1.2f
                    )
                }

                is Wall.HorizontalLine -> {
                    renderScaledSprite(
                        position = wall.startPosition,
                        cellSize = cellSize,
                        sprite = sprites.leftWall,
                        scale = 1.2f
                    )
                    if (wall.startPosition.x + 1 < wall.endPosition.x) {
                        drawImage(
                            image = sprites.horizontalWall,
                            dstOffset = IntOffset(
                                x = ((wall.startPosition.x + 1) * cellSize).toInt(),
                                y = (wall.startPosition.y * cellSize - cellSize * 0.1f).toInt()
                            ),
                            dstSize = IntSize(
                                width = (cellSize * (wall.endPosition.x - wall.startPosition.x - 1)).toInt(),
                                height = (cellSize * 1.2f).toInt()
                            )
                        )
                    }
                    renderScaledSprite(
                        position = wall.endPosition,
                        cellSize = cellSize,
                        sprite = sprites.rightWall,
                        scale = 1.2f
                    )
                }

                is Wall.VerticalLine -> {
                    renderScaledSprite(
                        position = wall.startPosition,
                        cellSize = cellSize,
                        sprite = sprites.topWall,
                        scale = 1.2f
                    )
                    if (wall.startPosition.y + 1 < wall.endPosition.y) {
                        drawImage(
                            image = sprites.verticalWall,
                            dstOffset = IntOffset(
                                x = (wall.startPosition.x * cellSize - cellSize * 0.1f).toInt(),
                                y = ((wall.startPosition.y + 1) * cellSize).toInt()
                            ),
                            dstSize = IntSize(
                                width = (cellSize * 1.2f).toInt(),
                                height = ((wall.endPosition.y - wall.startPosition.y - 1) * cellSize).toInt()
                            )
                        )
                    }
                    renderScaledSprite(
                        position = wall.endPosition,
                        cellSize = cellSize,
                        sprite = sprites.bottomWall,
                        scale = 1.2f
                    )
                }
            }
        }
    }

    private fun DrawScope.renderScaledSprite(
        position: Position,
        cellSize: Float,
        sprite: ImageBitmap,
        scale: Float
    ) {
        drawImage(
            image = sprite,
            dstOffset = IntOffset(
                x = (position.x * cellSize - cellSize * (scale - 1) / 2).toInt(),
                y = (position.y * cellSize - cellSize * (scale - 1) / 2).toInt()
            ),
            dstSize = IntSize(
                width = (cellSize * scale).toInt(),
                height = (cellSize * scale).toInt()
            )
        )
    }
}

@Composable
fun rememberSnakeGameRenderer(snakeSkin: SnakeSkin): SnakeGameRenderer {
    val sprites = SnakeGameSprites(
        redApple = ImageBitmap.imageResource(R.drawable.apple_red_64),
        greenApple = ImageBitmap.imageResource(R.drawable.apple_green_64),
        goldApple = ImageBitmap.imageResource(R.drawable.apple_gold_64),
        badApple = ImageBitmap.imageResource(R.drawable.oliebol_64),
        coin = ImageBitmap.imageResource(R.drawable.coin),
        diamond = ImageBitmap.imageResource(R.drawable.diamond),
        mainSnakeBody = ImageBitmap.imageResource(snakeSkin.bodyResId),
        mainSnakeHead = ImageBitmap.imageResource(snakeSkin.headResId),
        mainSnakeHeadXX = ImageBitmap.imageResource(snakeSkin.headXXResId),
        secondarySnakeBody = ImageBitmap.imageResource(R.drawable.snake_yellow_blob_64),
        secondarySnakeHead = ImageBitmap.imageResource(R.drawable.snake_yellow_head_64),
        secondarySnakeHeadXX = ImageBitmap.imageResource(R.drawable.snake_yellow_xx),
        easterEgg = ImageBitmap.imageResource(R.drawable.easter_egg_64),
        bomb = ImageBitmap.imageResource(R.drawable.bomb_64),
        blockWall = ImageBitmap.imageResource(R.drawable.wall_block_64_0),
        leftWall = ImageBitmap.imageResource(R.drawable.wall_block_64_1),
        horizontalWall = ImageBitmap.imageResource(R.drawable.wall_block_64_2),
        rightWall = ImageBitmap.imageResource(R.drawable.wall_block_64_3),
        bottomWall = ImageBitmap.imageResource(R.drawable.wall_block_64_6),
        verticalWall = ImageBitmap.imageResource(R.drawable.wall_block_64_5),
        topWall = ImageBitmap.imageResource(R.drawable.wall_block_64_4)
    )
    return remember(snakeSkin) { SnakeGameRenderer(sprites) }
}

private class PreviewGameIsOverProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean>
        get() = sequenceOf(false, true)

}

@Preview
@Composable
private fun Preview(
    @PreviewParameter(provider = PreviewGameIsOverProvider::class) gameIsOver: Boolean
) {
    val renderer = rememberSnakeGameRenderer(SnakeSkin.DEFAULT)
    val model = SnakeGameModel(
        gridWidth = 15,
        gridHeight = 30,
        apples = sequenceOf(
            Apple(
                position = Position(x = 1, y = 1),
                type = AppleType.GOOD_1
            ),
            Apple(
                position = Position(x = 3, y = 1),
                type = AppleType.GOOD_2
            ),
            Apple(
                position = Position(x = 5, y = 1),
                type = AppleType.BAD
            ),
            Apple(
                position = Position(x = 7, y = 1),
                type = AppleType.OMNIVOROUSNESS
            ),
            Apple(
                position = Position(x = 9, y = 1),
                type = AppleType.BOMB
            ),
            Apple(
                position = Position(x = 11, y = 1),
                type = AppleType.GOLDEN
            )
        ),
        snakes = listOf(
            SnakeModel(
                elements = sequenceOf(
                    Position(x = 3, y = 4),
                    Position(x = 4, y = 4),
                    Position(x = 5, y = 4),
                    Position(x = 6, y = 4),
                    Position(x = 6, y = 5),
                    Position(x = 6, y = 6),
                    Position(x = 6, y = 7),
                ),
                type = SnakeType.MAIN,
                omnivorousTicksRemaining = 0,
                direction = Direction.DOWN,
                length = 7
            ),
            SnakeModel(
                elements = sequenceOf(
                    Position(x = 10, y = 4),
                    Position(x = 11, y = 4),
                    Position(x = 12, y = 4),
                    Position(x = 12, y = 5),
                    Position(x = 12, y = 6),
                    Position(x = 12, y = 7),
                ),
                type = SnakeType.SECONDARY,
                omnivorousTicksRemaining = 1,
                direction = Direction.DOWN,
                length = 7
            )
        ),
        walls = listOf(
            Wall.SingleBlock(
                position = Position(x = 2, y = 10)
            ),
            Wall.HorizontalLine(
                startPosition = Position(x = 2, y = 12),
                endPosition = Position(x = 6, y = 12)
            ),
            Wall.VerticalLine(
                startPosition = Position(x = 8, y = 12),
                endPosition = Position(x = 8, y = 17)
            )
        ),
        gameIsOver = gameIsOver,
        level = 1,
        remainingLengthToGainLevel = 0,
        appleCount = 0,
        score = 123
    )
    Canvas(Modifier.fillMaxSize()) {
        renderer.renderSnakeGame(model)
    }
}
