package com.alexey.kozyakov.snake.storage.model

import android.content.Context
import com.alexey.kozyakov.snake.levels.levelWalls
import com.alexey.kozyakov.snake.model.Apple
import com.alexey.kozyakov.snake.model.AppleType
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.model.Position
import com.alexey.kozyakov.snake.model.SnakeGameModel
import com.alexey.kozyakov.snake.model.SnakeModel
import com.alexey.kozyakov.snake.model.SnakeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

class SnakeGameModelFileSaver(context: Context) {
    private val saveFile = File(context.filesDir, "snake_game.save")

    suspend fun save(model: SnakeGameModel) = withContext(Dispatchers.IO) {
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
            }
        }
    }

    suspend fun load() = withContext(Dispatchers.IO) {
        if (!saveFile.exists()) {
            null
        } else {
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
                        val elements = List(length) {
                            val x = readInt()
                            val y = readInt()
                            Position(x, y)
                        }
                        SnakeModel(
                            elements = elements.asSequence(),
                            direction = direction,
                            length = length,
                            type = if (it == 0) SnakeType.MAIN else SnakeType.SECONDARY,
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
                    SnakeGameModel(
                        gridWidth = gridWidth,
                        gridHeight = gridHeight,
                        apples = apples.asSequence(),
                        snakes = snakes,
                        walls = levelWalls(level, gridWidth, gridHeight),
                        gameIsOver = false,
                        level = level,
                        remainingLengthToGainLevel = 0,
                        appleCount = appleCount,
                        score = score
                    )
                }
            }
        }
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        saveFile.delete()
    }
}
