package com.alexey.kozyakov.snake.storage.model

import android.content.Context
import com.alexey.kozyakov.snake.model.Apple
import com.alexey.kozyakov.snake.model.AppleType
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.model.Position
import com.alexey.kozyakov.snake.model.SnakeGameModel
import com.alexey.kozyakov.snake.model.SnakeModel
import com.alexey.kozyakov.snake.model.SnakeType
import com.alexey.kozyakov.snake.model.Wall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

class SnakeGameModelFileSaver(context: Context) {
    private val saveFile = File(context.filesDir, "snake_game.save")

    suspend fun save(model: SnakeGameModel) = withContext(Dispatchers.IO) {
        require(!model.gameIsOver) { "Cannot save model in game over state" }
        saveFile.createNewFile()
        DataOutputStream(saveFile.outputStream()).use { outputStream ->
            with(outputStream) {
                writeInt(model.gridWidth)
                writeInt(model.gridHeight)
                writeInt(model.level)
                writeInt(model.score)
                writeInt(model.snakes.size)
                for (snake in model.snakes) {
                    writeInt(snake.length)
                    writeInt(snake.direction.ordinal)
                    writeInt(snake.omnivorousTicksRemaining)
                    writeInt(snake.type.ordinal)
                    for (element in snake.elements) {
                        writeInt(element.x)
                        writeInt(element.y)
                    }
                }
                writeInt(model.appleCount)
                for (apple in model.apples) {
                    writeInt(apple.type.ordinal)
                    writeInt(apple.position.x)
                    writeInt(apple.position.y)
                }
                writeInt(model.walls.size)
                for (wall in model.walls) {
                    when (wall) {
                        is Wall.SingleBlock -> {
                            writeInt(Wall.SingleBlock.ORDINAL)
                            writeInt(wall.position.x)
                            writeInt(wall.position.y)
                        }

                        is Wall.HorizontalLine -> {
                            writeInt(Wall.HorizontalLine.ORDINAL)
                            writeInt(wall.startPosition.x)
                            writeInt(wall.startPosition.y)
                            writeInt(wall.endPosition.x)
                            writeInt(wall.endPosition.y)
                        }

                        is Wall.VerticalLine -> {
                            writeInt(Wall.VerticalLine.ORDINAL)
                            writeInt(wall.startPosition.x)
                            writeInt(wall.startPosition.y)
                            writeInt(wall.endPosition.x)
                            writeInt(wall.endPosition.y)
                        }
                    }
                }
            }
        }
    }

    suspend fun load() = withContext(Dispatchers.IO) {
        if (!saveFile.exists()) {
            null
        } else {
            try {
                DataInputStream(saveFile.inputStream()).use { dataInputStream ->
                    with(dataInputStream) {
                        val gridWidth = readInt()
                        val gridHeight = readInt()
                        val level = readInt()
                        val score = readInt()
                        val snakesCount = readInt()
                        val snakes = List(snakesCount) {
                            val length = readInt()
                            val direction = Direction.entries[readInt()]
                            val omnivorousTicksRemaining = readInt()
                            val type = SnakeType.entries[readInt()]
                            val elements = List(length) {
                                val x = readInt()
                                val y = readInt()
                                Position(x, y)
                            }
                            SnakeModel(
                                elements = elements.asSequence(),
                                direction = direction,
                                length = length,
                                type = type,
                                omnivorousTicksRemaining = omnivorousTicksRemaining
                            )
                        }
                        val appleCount = readInt()
                        val apples = List(appleCount) {
                            val type = AppleType.entries[readInt()]
                            val x = readInt()
                            val y = readInt()
                            Apple(position = Position(x, y), type = type)
                        }
                        val wallsCount = readInt()
                        val walls = List(wallsCount) {
                            when (val ordinal = readInt()) {
                                Wall.SingleBlock.ORDINAL -> {
                                    val x = readInt()
                                    val y = readInt()
                                    Wall.SingleBlock(position = Position(x = x, y = y))
                                }

                                Wall.HorizontalLine.ORDINAL -> {
                                    val xStart = readInt()
                                    val yStart = readInt()
                                    val xEnd = readInt()
                                    val yEnd = readInt()
                                    Wall.HorizontalLine(
                                        startPosition = Position(x = xStart, y = yStart),
                                        endPosition = Position(x = xEnd, y = yEnd)
                                    )
                                }

                                Wall.VerticalLine.ORDINAL -> {
                                    val xStart = readInt()
                                    val yStart = readInt()
                                    val xEnd = readInt()
                                    val yEnd = readInt()
                                    Wall.VerticalLine(
                                        startPosition = Position(x = xStart, y = yStart),
                                        endPosition = Position(x = xEnd, y = yEnd)
                                    )
                                }

                                else -> throw IllegalStateException("Unsupported wall type: $ordinal")
                            }
                        }
                        SnakeGameModel(
                            gridWidth = gridWidth,
                            gridHeight = gridHeight,
                            apples = apples.asSequence(),
                            snakes = snakes,
                            walls = walls,
                            gameIsOver = false,
                            level = level,
                            remainingLengthToGainLevel = 0,
                            appleCount = appleCount,
                            score = score
                        )
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        saveFile.delete()
    }
}
