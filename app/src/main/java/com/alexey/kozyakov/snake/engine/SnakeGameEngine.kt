package com.alexey.kozyakov.snake.engine

import com.alexey.kozyakov.snake.config.ADDITIONAL_SCORE_FOR_GOLDEN_APPLE
import com.alexey.kozyakov.snake.config.ADDITIONAL_SCORE_FOR_OMNIVOROUS_APPLE
import com.alexey.kozyakov.snake.config.APPLE_COUNT
import com.alexey.kozyakov.snake.config.INITIAL_SNAKE_LENGTH
import com.alexey.kozyakov.snake.config.LEVEL_GAIN_LENGTH_MULTIPLIER
import com.alexey.kozyakov.snake.config.OMNIVOROUS_TICKS
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
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random


interface SnakeGameEngine {
    val model: SnakeGameModel
    fun step(): List<SnakeGameEvent>
    fun transposeGrid()
    fun setDirection(snakeId: Int = MAIN_SNAKE_ID, newDirection: Direction): Boolean
    fun restartFinishedGame(): Boolean

    companion object {
        fun empty(): SnakeGameEngine = EmptySnakeGameEngine

        fun create(
            gridWidth: Int,
            gridHeight: Int,
            boostersSupplier: SnakeBoostersSupplier = SnakeBoostersSupplier.Empty
        ): SnakeGameEngine {
            return SnakeGameEngineImpl(
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                boostersSupplier = boostersSupplier
            )
        }

        fun restore(
            model: SnakeGameModel,
            boostersSupplier: SnakeBoostersSupplier = SnakeBoostersSupplier.Empty
        ): SnakeGameEngine {
            val initialSnakes = model.snakes.map { snakeModel ->
                val elementsIterator = snakeModel.elements.iterator()
                val tail = SnakeElement(position = elementsIterator.next())
                var head = tail
                while (elementsIterator.hasNext()) {
                    head.next = SnakeElement(position = elementsIterator.next())
                    head = head.next!!
                }
                Snake(
                    head = head,
                    tail = tail,
                    direction = snakeModel.direction,
                    committedDirection = snakeModel.direction,
                    length = snakeModel.length,
                    omnivorousTicksRemaining = snakeModel.omnivorousTicksRemaining
                )
            }
            val initialApples = mutableMapOf<Position, Apple>()
            for (apple in model.apples) {
                initialApples[apple.position] = apple
            }
            return SnakeGameEngineImpl(
                gridWidth = model.gridWidth,
                gridHeight = model.gridHeight,
                boostersSupplier = boostersSupplier,
                initialLevel = model.level,
                initialWalls = model.walls,
                initialSnakes = initialSnakes,
                initialApples = initialApples,
                initialScore = model.score
            )
        }
    }
}

private object EmptySnakeGameEngine : SnakeGameEngine {
    override val model = SnakeGameModel(
        gridWidth = 0,
        gridHeight = 0,
        apples = emptySequence(),
        snakes = emptyList(),
        walls = emptyList(),
        gameIsOver = false,
        level = 0,
        remainingLengthToGainLevel = 0,
        appleCount = 0,
        score = 0
    )

    override fun step() = emptyList<SnakeGameEvent>()

    override fun transposeGrid() = Unit

    override fun setDirection(
        snakeId: Int,
        newDirection: Direction
    ) = false

    override fun restartFinishedGame() = false
}

private const val MAIN_SNAKE_ID = 0
private const val AI_SNAKE_ID = 1

private class SnakeGameEngineImpl(
    private var gridWidth: Int,
    private var gridHeight: Int,
    private val boostersSupplier: SnakeBoostersSupplier = SnakeBoostersSupplier.Empty,
    initialLevel: Int? = null,
    initialWalls: List<Wall>? = null,
    initialSnakes: List<Snake>? = null,
    initialApples: MutableMap<Position, Apple>? = null,
    initialScore: Int? = null
) : SnakeGameEngine {
    private val initialSnakeLength = INITIAL_SNAKE_LENGTH
    private val appleCount = APPLE_COUNT

    private var level: Int = initialLevel ?: 0
    private var walls = initialWalls ?: levelWalls(level, gridWidth, gridHeight)
    private var snakes = initialSnakes ?: initSnakes()
    private var apples = initialApples ?: initApples(snakes, walls)
    private var score = initialScore ?: 0
    private var gameIsOver = false

    private val stepEvents = mutableListOf<SnakeGameEvent>()

    private val remainingLengthToGainLevel: Int
        get() {
            val minGridDimension = min(gridWidth, gridHeight)
            val lengthToGainLevel =
                (minGridDimension * LEVEL_GAIN_LENGTH_MULTIPLIER.pow(level)).toInt()
            return lengthToGainLevel - snakes[MAIN_SNAKE_ID].length
        }

    private val snakeAi = SnakeAI(
        engine = this,
        snakeId = AI_SNAKE_ID
    )

    init {
        require(initialSnakeLength > 1)
        require(appleCount > 0)
    }

    override val model: SnakeGameModel
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
                    direction = snake.committedDirection
                )
            },
            gameIsOver = gameIsOver,
            level = level,
            walls = walls,
            remainingLengthToGainLevel = remainingLengthToGainLevel,
            appleCount = appleCount,
            score = score,
        )

    override fun step(): List<SnakeGameEvent> {
        if (gameIsOver) {
            return emptyList()
        }
        stepEvents.clear()
        snakeAi.step()
        doStep()
        applyPostStepActions()
        return if (stepEvents.isEmpty()) emptyList() else stepEvents.toMutableList()
    }

    override fun transposeGrid() {
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

    override fun setDirection(snakeId: Int, newDirection: Direction): Boolean {
        if (gameIsOver) {
            return false
        }
        val snake = snakes[snakeId]
        if (newDirection.isOpposite(snake.committedDirection)) {
            return false
        }
        snake.direction = newDirection
        return true
    }

    override fun restartFinishedGame(): Boolean {
        if (!gameIsOver) {
            return false
        }
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
                element.position = element.next!!.position
                element = element.next!!
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
                addStepEvent(snakeId, SnakeGameEvent.SNAKE_GROWS)
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
                            addStepEvent(snakeId, SnakeGameEvent.BAD_APPLE_EATEN)
                        }
                    }

                    AppleType.OMNIVOROUSNESS -> {
                        grow()
                        addScore(snakeId, ADDITIONAL_SCORE_FOR_OMNIVOROUS_APPLE)
                        snake.omnivorousTicksRemaining = OMNIVOROUS_TICKS
                        addStepEvent(snakeId, SnakeGameEvent.OMNIVOROUS_APPLE_EATEN)
                    }

                    AppleType.BOMB -> {
                        if (snake.omnivorousTicksRemaining > 0) {
                            grow()
                        } else {
                            val newSnakeLength =
                                if (snake.length > initialSnakeLength) {
                                    initialSnakeLength
                                } else {
                                    if (isMainSnake(snakeId)) 1 else snake.length
                                }
                            shrinkSnake(snake, newSnakeLength)
                            addStepEvent(snakeId, SnakeGameEvent.BOMB_EATEN)
                        }
                    }

                    AppleType.GOLDEN -> {
                        grow()
                        addScore(snakeId, ADDITIONAL_SCORE_FOR_GOLDEN_APPLE)
                        goldAppleEaten = true
                        addStepEvent(snakeId, SnakeGameEvent.GOLDEN_APPLE_EATEN)
                    }

                    AppleType.COIN -> {
                        addStepEvent(snakeId, SnakeGameEvent.COIN_PICKED)
                    }

                    AppleType.DIAMOND -> {
                        addStepEvent(snakeId, SnakeGameEvent.DIAMOND_PICKED)
                    }
                }
            }

            val touchedWalls = walls.filter { wall -> wall.containsPosition(head.position) }
            if (touchedWalls.isNotEmpty() && isMainSnake(snakeId)) {
                if (boostersSupplier.consumeWallEatingBooster()) {
                    grow()
                    addStepEvent(snakeId, SnakeGameEvent.WALL_EATEN)
                    val splittedWalls = touchedWalls.flatMap { wall -> wall.splitBy(head.position) }
                    walls = walls - touchedWalls.toSet() + splittedWalls
                } else {
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

    private fun addStepEvent(snakeId: Int, event: SnakeGameEvent) {
        if (isMainSnake(snakeId)) {
            stepEvents.add(event)
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
            addStepEvent(MAIN_SNAKE_ID, SnakeGameEvent.LEVEL_GAINED)
            return
        }
        snakes.forEachIndexed { snakeId, snake ->
            snake.committedDirection = snake.direction
            if (snake.omnivorousTicksRemaining > 0) {
                snake.omnivorousTicksRemaining--
            }
            if (!isMainSnake(snakeId) && !checkCollisionWithSelfOrOtherSnakes(snakeId).isNone) {
                shrinkSnake(snake, initialSnakeLength)
                snake.omnivorousTicksRemaining = 0
            }
            if (isMainSnake(snakeId)) {
                if (snake.length < 2) {
                    gameOver()
                } else {
                    when (val collision = checkCollisionWithSelfOrOtherSnakes(snakeId)) {
                        is Collision.Self -> {
                            gameOver()
                        }

                        is Collision.Other -> {
                            if (collision.isHead) {
                                gameOver()
                            } else {
                                if (boostersSupplier.consumeSnakeEatingBooster()) {
                                    val head = snakes[snakeId].head
                                    val eatenLength = collision.snakes.sumOf { snake ->
                                        shrinkSnakeToPosition(snake, head.position)
                                    }
                                    addScore(snakeId, eatenLength)
                                    addStepEvent(snakeId, SnakeGameEvent.SNAKE_PART_EATEN)
                                } else {
                                    gameOver()
                                }
                            }
                        }

                        Collision.None -> Unit
                    }
                }
            }
        }
    }

    private fun isMainSnake(snakeId: Int): Boolean {
        return snakeId == MAIN_SNAKE_ID
    }

    private fun gameOver() {
        gameIsOver = true
        addStepEvent(MAIN_SNAKE_ID, SnakeGameEvent.GAME_OVER)
    }

    private fun checkCollisionWithSelfOrOtherSnakes(snakeId: Int): Collision {
        val snake = snakes[snakeId]
        val snakeHead = snake.head
        if (isInSnake(snake.tail, snakeHead.position, includeHead = false)) {
            return Collision.Self
        }
        val otherSnakes = snakes.filterIndexed { id, _ -> id != snakeId }
        val touchedSnakes = otherSnakes.filter { snake ->
            isInSnake(snake.tail, snakeHead.position)
        }
        if (touchedSnakes.isNotEmpty()) {
            return Collision.Other(
                snakes = touchedSnakes,
                isHead = touchedSnakes.any { snake -> snake.head.position == snakeHead.position }
            )
        }
        return Collision.None
    }

    private fun shrinkSnake(snake: Snake, targetLength: Int) {
        while (snake.length > targetLength && snake.tail.next != null) {
            snake.tail = snake.tail.next!!
            snake.length--
        }
    }

    private fun shrinkSnakeToPosition(snake: Snake, position: Position): Int {
        var removedLength = 0
        var lastTailPosition: Position
        do {
            lastTailPosition = snake.tail.position
            snake.tail = snake.tail.next!!
            snake.length--
            removedLength++
        } while (lastTailPosition != position)
        return removedLength
    }

    private fun initSnakes(): MutableList<Snake> {
        val direction = if (gridWidth > gridHeight) {
            Direction.DOWN
        } else {
            Direction.RIGHT
        }
        return mutableListOf(
            initSnake(
                direction = direction,
                offset = 0
            ),
            initSnake(
                direction = direction,
                offset = -4
            )
        )
    }

    private fun initSnake(
        direction: Direction,
        offset: Int
    ): Snake {
        val position = Position(
            x = if (gridWidth > gridHeight) gridWidth / 2 + offset else gridWidth / 2,
            if (gridWidth > gridHeight) gridHeight / 2 else gridHeight / 2 + offset
        )
        var tail = SnakeElement(position)
        val head = tail
        repeat(initialSnakeLength - 1) {
            tail = SnakeElement(
                position = Position(
                    x = tail.position.x - direction.dx,
                    y = tail.position.y - direction.dy
                ),
                next = tail
            )
        }
        return Snake(
            head = head,
            tail = tail,
            direction = direction,
            committedDirection = direction,
            length = initialSnakeLength,
            omnivorousTicksRemaining = 0
        )
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
            in 13..33 -> AppleType.BAD
            in 34..38 -> AppleType.COIN
            in 39..39 -> AppleType.DIAMOND
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
}

private class SnakeElement(
    var position: Position,
    var next: SnakeElement? = null
)

private class Snake(
    val head: SnakeElement,
    var tail: SnakeElement,
    var direction: Direction,
    var committedDirection: Direction,
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
        committedDirection = committedDirection.transposed()
    }
}

private sealed interface Collision {
    object None : Collision
    object Self : Collision
    class Other(val snakes: List<Snake>, val isHead: Boolean) : Collision
}

private val Collision.isNone get() = this == Collision.None
