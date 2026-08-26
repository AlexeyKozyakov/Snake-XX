package com.alexey.kozyakov.snake.engine

import com.alexey.kozyakov.snake.config.ADDITIONAL_SCORE_FOR_GOLDEN_APPLE
import com.alexey.kozyakov.snake.config.ADDITIONAL_SCORE_FOR_OMNIVOROUS_APPLE
import com.alexey.kozyakov.snake.levels.levelWalls
import com.alexey.kozyakov.snake.model.Apple
import com.alexey.kozyakov.snake.model.AppleType
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.model.Position
import com.alexey.kozyakov.snake.model.SnakeGameEvent
import com.alexey.kozyakov.snake.model.SnakeGameModel
import com.alexey.kozyakov.snake.model.SnakeModel
import com.alexey.kozyakov.snake.model.SnakeType
import com.alexey.kozyakov.snake.model.Wall
import com.alexey.kozyakov.snake.model.isGoodOrBonus
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

private const val MAIN_SNAKE_ID = 0
private const val AI_SNAKE_ID = 1

class SnakeGameEngine private constructor(
    private var gridWidth: Int,
    private var gridHeight: Int,
    private val initialSnakeLength: Int,
    private val appleCount: Int,
    private val omnivorousTicks: Int,
    aiFailProbabilityDefault: Double,
    aiFailDecreaseByLevelRatio: Double,
    private val levelGainLengthMultiplier: Double,
    initialLevel: Int? = null,
    initialWalls: List<Wall>? = null,
    initialSnakes: List<Snake>? = null,
    initialApples: MutableMap<Position, Apple>? = null,
    initialScore: Int? = null
) {
    private var level: Int = initialLevel ?: 0
    private var walls = initialWalls ?: levelWalls(level, gridWidth, gridHeight)
    private var snakes = initialSnakes ?: initSnakes()
    private var apples = initialApples ?: initApples(snakes, walls)
    private var score = initialScore ?: 0
    private var gameIsOver = false

    private val remainingLengthToGainLevel: Int
        get() {
            val minGridDimension = min(gridWidth, gridHeight)
            val lengthToGainLevel =
                (minGridDimension * levelGainLengthMultiplier.pow(level)).toInt()
            return lengthToGainLevel - snakes[MAIN_SNAKE_ID].length
        }

    private val snakeAi = SnakeAI(
        engine = this,
        snakeId = AI_SNAKE_ID,
        aiFailProbabilityDefault = aiFailProbabilityDefault,
        aiFailDecreaseByLevelRatio = aiFailDecreaseByLevelRatio,
    )

    init {
        require(initialSnakeLength > 1)
        require(appleCount > 0)
    }

    val model: SnakeGameModel
        get() = SnakeGameModel(
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            apples = apples.values.asSequence(),
            snakes = snakes.mapIndexed { snakeId, snake ->
                SnakeModel(
                    elements = generateSequence(snake.tail) {
                        it.next
                    }.map {
                        it.position
                    },
                    type = if (isMainSnake(snakeId)) SnakeType.MAIN else SnakeType.SECONDARY,
                    length = snake.length,
                    omnivorousTicksRemaining = snake.omnivorousTicksRemaining,
                    direction = snake.commitedDirection
                )
            },
            gameIsOver = gameIsOver,
            level = level,
            walls = walls,
            remainingLengthToGainLevel = remainingLengthToGainLevel,
            appleCount = appleCount,
            score = score,
        )

    val events: List<SnakeGameEvent>
        field = mutableListOf()

    fun step() {
        if (gameIsOver) {
            return
        }
        events.clear()
        snakeAi.step()
        doStep()
        applyPostStepActions()
    }

    fun transposeGrid() {
        val tmp = gridWidth
        gridWidth = gridHeight
        gridHeight = tmp
        walls = walls.map { wall -> wall.transposed() }
        for (snake in snakes) {
            snake.transpose()
        }
        val oldApples = apples.values.toList()
        apples.clear()
        for (apple in oldApples) {
            apple.transpose()
            apples[apple.position] = apple
        }
    }

    fun setDirection(snakeId: Int = MAIN_SNAKE_ID, newDirection: Direction): Boolean {
        if (gameIsOver) {
            return false
        }
        val snake = snakes[snakeId]
        if (newDirection.isOpposite(snake.commitedDirection)) {
            return false
        }
        snake.direction = newDirection
        return true
    }

    fun restartFinishedGame(): Boolean {
        if (!gameIsOver) {
            return false
        }
        events.clear()
        level = 0
        score = 0
        walls = levelWalls(level, gridWidth, gridHeight)
        snakes = initSnakes()
        apples = initApples(snakes, walls)
        gameIsOver = false
        return true
    }

    private fun doStep() {
        var goldAppleEaten = false
        snakes.forEachIndexed { snakeId, snake ->
            val oldTailPosition = snake.tail.position
            var element = snake.tail
            while (element.next != null) {
                element.position = element.next.position
                element = element.next
            }
            val head = element
            head.position = Position(
                x = (head.position.x + snake.direction.dx + gridWidth) % gridWidth,
                y = (head.position.y + snake.direction.dy + gridHeight) % gridHeight
            )
            fun grow() {
                snake.tail = SnakeElement(
                    position = oldTailPosition,
                    next = snake.tail
                )
                snake.length++
                addScore(snakeId, 1)
                addEvent(snakeId, SnakeGameEvent.SNAKE_GROWS)
            }
            apples.remove(head.position)?.let { eatenApple ->
                when (eatenApple.type) {
                    AppleType.GOOD_1, AppleType.GOOD_2 -> {
                        grow()
                    }

                    AppleType.BAD -> {
                        if (snake.omnivorousTicksRemaining > 0) {
                            grow()
                        } else {
                            if (isMainSnake(snakeId) || snake.length > initialSnakeLength) {
                                snake.tail.next?.let {
                                    snake.tail = it
                                    snake.length--
                                }
                            }
                            addEvent(snakeId, SnakeGameEvent.BAD_APPLE_EATEN)
                        }
                    }

                    AppleType.OMNIVOROUSNESS -> {
                        grow()
                        addScore(snakeId, ADDITIONAL_SCORE_FOR_OMNIVOROUS_APPLE)
                        snake.omnivorousTicksRemaining = omnivorousTicks
                        addEvent(snakeId, SnakeGameEvent.OMNIVOROUS_APPLE_EATEN)
                    }

                    AppleType.BOMB -> {
                        if (snake.omnivorousTicksRemaining > 0) {
                            grow()
                        } else {
                            val newSnakeLength =
                                if (snake.length > initialSnakeLength || !isMainSnake(snakeId)) {
                                    initialSnakeLength
                                } else {
                                    1
                                }
                            shrinkSnake(snake, newSnakeLength)
                            addEvent(snakeId, SnakeGameEvent.BOMB_EATEN)
                        }
                    }

                    AppleType.GOLDEN -> {
                        grow()
                        addScore(snakeId, ADDITIONAL_SCORE_FOR_GOLDEN_APPLE)
                        goldAppleEaten = true
                        addEvent(snakeId, SnakeGameEvent.GOLDEN_APPLE_EATEN)
                    }
                }
            }

            if (walls.any { wall -> wall.containsPosition(head.position) }) {
                if (isMainSnake(snakeId)) {
                    gameOver()
                }
            }
        }
        repeat(appleCount - apples.size) {
            initNewApple(apples, snakes, walls)
        }
        if (goldAppleEaten) {
            apples.values.forEach { apple ->
                if (!apple.type.isGoodOrBonus) {
                    apple.type = randomGoodAppleType()
                }
            }
        }
    }

    private fun addScore(snakeId: Int, value: Int) {
        if (isMainSnake(snakeId)) {
            score += value
        }
    }

    private fun addEvent(snakeId: Int, event: SnakeGameEvent) {
        if (isMainSnake(snakeId)) {
            events.add(event)
        }
    }

    private fun randomGoodAppleType(): AppleType {
        return if (Random.nextBoolean()) {
            AppleType.GOOD_1
        } else {
            AppleType.GOOD_2
        }
    }

    private fun applyPostStepActions() {
        val oldLevel = level
        if (remainingLengthToGainLevel <= 0) {
            level++
        }
        val levelGained = level > oldLevel
        if (levelGained) {
            addScore(MAIN_SNAKE_ID, snakes[MAIN_SNAKE_ID].length - initialSnakeLength)
            walls = levelWalls(level, gridWidth, gridHeight)
            snakes = initSnakes()
            apples = initApples(snakes, walls)
            addEvent(MAIN_SNAKE_ID, SnakeGameEvent.LEVEL_GAINED)
            return
        }
        snakes.forEachIndexed { snakeId, snake ->
            snake.commitedDirection = snake.direction
            if (snake.omnivorousTicksRemaining > 0) {
                snake.omnivorousTicksRemaining--
            }
            if (!isMainSnake(snakeId) && checkCollisionWithSelfOrOtherSnakes(snakeId)) {
                shrinkSnake(snake, initialSnakeLength)
                snake.omnivorousTicksRemaining = 0
            }
            if (isMainSnake(snakeId) &&
                (snake.length < 2 || checkCollisionWithSelfOrOtherSnakes(snakeId))
            ) {
                gameOver()
            }
        }
    }

    private fun isMainSnake(snakeId: Int): Boolean {
        return snakeId == MAIN_SNAKE_ID
    }

    private fun gameOver() {
        gameIsOver = true
        addEvent(MAIN_SNAKE_ID, SnakeGameEvent.GAME_OVER)
    }

    private fun checkCollisionWithSelfOrOtherSnakes(snakeId: Int): Boolean {
        val snake = snakes[snakeId]
        var snakeHead: SnakeElement = snake.tail
        while (snakeHead.next != null) {
            snakeHead = snakeHead.next
        }
        return isInSnake(snake.tail, snakeHead.position, includeHead = false) ||
                snakes
                    .filterIndexed { id, _ -> id != snakeId }
                    .any { snake -> isInSnake(snake.tail, snakeHead.position) }
    }

    private fun shrinkSnake(snake: Snake, targetLength: Int) {
        while (snake.length > targetLength && snake.tail.next != null) {
            snake.tail = snake.tail.next!!
            snake.length--
        }
    }

    private fun initSnakes(): MutableList<Snake> {
        val direction = if (gridWidth > gridHeight) {
            Direction.DOWN
        } else {
            Direction.RIGHT
        }
        return mutableListOf(
            Snake(
                tail = initSnakeElements(direction = direction, offset = 0),
                direction = direction,
                commitedDirection = direction,
                length = initialSnakeLength,
                omnivorousTicksRemaining = 0
            ),
            Snake(
                tail = initSnakeElements(direction = direction, offset = -4),
                direction = direction,
                commitedDirection = direction,
                length = initialSnakeLength,
                omnivorousTicksRemaining = 0
            )
        )
    }

    private fun initSnakeElements(direction: Direction, offset: Int): SnakeElement {
        val position = Position(
            x = if (gridWidth > gridHeight) gridWidth / 2 + offset else gridWidth / 2,
            if (gridWidth > gridHeight) gridHeight / 2 else gridHeight / 2 + offset
        )
        var element = SnakeElement(position)
        repeat(initialSnakeLength - 1) {
            element = SnakeElement(
                position = Position(
                    x = element.position.x - direction.dx,
                    y = element.position.y - direction.dy
                ),
                next = element
            )
        }
        return element
    }

    private fun initApples(snakes: List<Snake>, walls: List<Wall>): MutableMap<Position, Apple> {
        val apples = mutableMapOf<Position, Apple>()
        repeat(appleCount) {
            initNewApple(apples, snakes, walls)
        }
        return apples
    }

    private fun initNewApple(
        apples: MutableMap<Position, Apple>,
        snakes: List<Snake>,
        walls: List<Wall>
    ) {
        var position: Position
        do {
            position = Position(
                x = Random.nextInt(0, gridWidth),
                y = Random.nextInt(0, gridHeight)
            )
        } while (
            position in apples
            || snakes.any { snake -> isInSnake(snake.tail, position) }
            || walls.any { wall -> wall.containsPosition(position) }
        )
        val seed = Random.nextInt(0, 100)
        val type = when (seed) {
            in 0..5 -> AppleType.OMNIVOROUSNESS
            in 6..12 -> AppleType.BOMB
            in 13..39 -> AppleType.BAD
            in 40..47 -> AppleType.GOLDEN
            in 48..73 -> AppleType.GOOD_1
            else -> AppleType.GOOD_2
        }
        val newApple = Apple(position, type)
        apples[position] = newApple
    }

    private fun isInSnake(
        snakeTail: SnakeElement,
        position: Position,
        includeHead: Boolean = true
    ): Boolean {
        var element: SnakeElement? = snakeTail
        while (element != null) {
            if (position == element.position && (includeHead || element.next != null)) {
                return true
            }
            element = element.next
        }
        return false
    }

    companion object {
        fun create(
            gridWidth: Int,
            gridHeight: Int,
            initialSnakeLength: Int,
            appleCount: Int,
            omnivorousTicks: Int,
            aiFailProbabilityDefault: Double,
            aiFailDecreaseByLevelRatio: Double,
            levelGainLengthMultiplier: Double
        ): SnakeGameEngine {
            return SnakeGameEngine(
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                initialSnakeLength = initialSnakeLength,
                appleCount = appleCount,
                omnivorousTicks = omnivorousTicks,
                aiFailProbabilityDefault = aiFailProbabilityDefault,
                aiFailDecreaseByLevelRatio = aiFailDecreaseByLevelRatio,
                levelGainLengthMultiplier = levelGainLengthMultiplier
            )
        }

        fun restore(
            model: SnakeGameModel,
            initialSnakeLength: Int,
            omnivorousTicks: Int,
            aiFailProbabilityDefault: Double,
            aiFailDecreaseByLevelRatio: Double,
            levelGainLengthMultiplier: Double,
        ): SnakeGameEngine {
            fun restoreSnakeElements(iterator: Iterator<Position>): SnakeElement? {
                if (!iterator.hasNext()) {
                    return null
                }
                return SnakeElement(
                    position = iterator.next(),
                    next = restoreSnakeElements(iterator)
                )
            }

            val initialSnakes = model.snakes.map { snakeModel ->
                Snake(
                    tail = restoreSnakeElements(snakeModel.elements.iterator())!!,
                    direction = snakeModel.direction,
                    commitedDirection = snakeModel.direction,
                    length = snakeModel.length,
                    omnivorousTicksRemaining = snakeModel.omnivorousTicksRemaining
                )
            }
            val initialApples = mutableMapOf<Position, Apple>()
            for (apple in model.apples) {
                initialApples[apple.position] = apple
            }
            return SnakeGameEngine(
                gridWidth = model.gridWidth,
                gridHeight = model.gridHeight,
                initialSnakeLength = initialSnakeLength,
                appleCount = initialApples.size,
                omnivorousTicks = omnivorousTicks,
                aiFailProbabilityDefault = aiFailProbabilityDefault,
                aiFailDecreaseByLevelRatio = aiFailDecreaseByLevelRatio,
                levelGainLengthMultiplier = levelGainLengthMultiplier,
                initialLevel = model.level,
                initialWalls = model.walls,
                initialSnakes = initialSnakes,
                initialApples = initialApples,
                initialScore = model.score
            )
        }
    }
}

private class SnakeElement(
    var position: Position,
    val next: SnakeElement? = null
)

private class Snake(
    var tail: SnakeElement,
    var direction: Direction,
    var commitedDirection: Direction,
    var length: Int,
    var omnivorousTicksRemaining: Int,
) {
    fun transpose() {
        var element: SnakeElement? = tail
        while (element != null) {
            element.position = element.position.transposed()
            element = element.next
        }
        direction = direction.transposed()
        commitedDirection = commitedDirection.transposed()
    }
}
