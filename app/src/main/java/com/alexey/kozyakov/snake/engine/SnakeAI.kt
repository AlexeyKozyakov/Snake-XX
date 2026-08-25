package com.alexey.kozyakov.snake.engine

import com.alexey.kozyakov.snake.model.AppleType
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.model.Position
import com.alexey.kozyakov.snake.model.isGoodOrBonus
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

class SnakeAI(
    val engine: SnakeGameEngine,
    val snakeId: Int,
    val aiFailProbabilityDefault: Double,
    val aiFailDecreaseByLevelRatio: Double,
) {
    fun step() {
        val model = engine.model
        val snake = model.snakes[snakeId]
        val snakeHead = snake.elements.last()
        val randomDirectionProbability =
            aiFailProbabilityDefault / aiFailDecreaseByLevelRatio.pow(model.level)
        val directionToTarget = if (Random.nextFloat() < randomDirectionProbability) {
            randomDirection()
        } else {
            val targetApple = model.apples
                .filter { apple ->
                    if (snake.omnivorousTicksRemaining > 0) {
                        apple.type != AppleType.GOLDEN
                    } else {
                        apple.type.isGoodOrBonus
                    }
                }
                .minByOrNull { apple ->
                    distance(apple.position, snakeHead)
                }?.position
            if (targetApple != null) {
                if (abs(targetApple.x - snakeHead.x) > abs(targetApple.y - snakeHead.y)) {
                    if (targetApple.x > snakeHead.x) {
                        Direction.RIGHT
                    } else {
                        Direction.LEFT
                    }
                } else {
                    if (targetApple.y > snakeHead.y) {
                        Direction.DOWN
                    } else {
                        Direction.UP
                    }
                }
            } else {
                randomDirection()
            }
        }
        val nextPosition =
            Position(x = snakeHead.x + directionToTarget.dx, y = snakeHead.y + directionToTarget.dy)
        val newDirection = if (
            model.snakes
                .any { snake -> nextPosition in snake.elements }
            || model.walls
                .any { wall -> wall.containsPosition(nextPosition) }
            || snake.omnivorousTicksRemaining <= 0 && model.apples
                .filter { apple -> !apple.type.isGoodOrBonus }
                .any { apple -> nextPosition == apple.position }
        ) {
            directionToTarget.opposite()
        } else {
            directionToTarget
        }
        val directionSet = engine.setDirection(snakeId, newDirection)
        if (!directionSet) {
            val anotherDirection = newDirection.nextClockwise()
            engine.setDirection(snakeId, anotherDirection)
        }
    }

    private fun distance(position1: Position, position2: Position): Int {
        return abs(position1.x - position2.x) + abs(position1.y - position2.y)
    }

    private fun randomDirection(): Direction {
        return Direction.entries[Random.nextInt(0, 4)]
    }
}
