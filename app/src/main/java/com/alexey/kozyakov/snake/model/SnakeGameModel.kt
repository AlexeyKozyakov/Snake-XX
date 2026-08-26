package com.alexey.kozyakov.snake.model

data class Position(val x: Int, val y: Int) {
    fun transposed(): Position {
        return Position(y, x)
    }
}

class SnakeGameModel(
    val gridWidth: Int,
    val gridHeight: Int,
    val apples: Sequence<Apple>,
    val snakes: List<SnakeModel>,
    val walls: List<Wall>,
    val gameIsOver: Boolean,
    val level: Int,
    val remainingLengthToGainLevel: Int,
    val appleCount: Int,
    val score: Int
)

enum class SnakeType {
    MAIN,
    SECONDARY
}

class SnakeModel(
    val elements: Sequence<Position>,
    val direction: Direction,
    val length: Int,
    val type: SnakeType,
    val omnivorousTicksRemaining: Int,
)

enum class Direction(val dx: Int, val dy: Int) {
    LEFT(dx = -1, dy = 0),
    UP(dx = 0, dy = -1),
    RIGHT(dx = 1, dy = 0),
    DOWN(dx = 0, dy = 1);

    fun isOpposite(other: Direction): Boolean {
        return dx == -other.dx && dy == -other.dy
    }

    fun transposed(): Direction {
        return entries.first { direction -> direction.dx == dy && direction.dy == dx }
    }

    fun nextClockwise(): Direction {
        return entries[(ordinal + 1) % entries.size]
    }

    fun opposite(): Direction {
        return entries.first { direction -> direction.isOpposite(this) }
    }
}

class Apple(
    var position: Position,
    var type: AppleType
) {
    fun transpose() {
        position = position.transposed()
    }
}

enum class AppleType {
    GOOD_1,
    GOOD_2,
    BAD,
    OMNIVOROUSNESS,
    BOMB,
    GOLDEN
}

val AppleType.isGoodOrBonus
    get() = when (this) {
        AppleType.GOOD_1, AppleType.GOOD_2, AppleType.GOLDEN, AppleType.OMNIVOROUSNESS -> true
        AppleType.BAD, AppleType.BOMB -> false
    }

val AppleType.isBonus
    get() = when (this) {
        AppleType.GOLDEN, AppleType.OMNIVOROUSNESS -> true
        AppleType.GOOD_1, AppleType.GOOD_2, AppleType.BAD, AppleType.BOMB -> false
    }

sealed interface Wall {
    fun containsPosition(position: Position): Boolean
    fun transposed(): Wall

    class SingleBlock(val position: Position) : Wall {
        override fun containsPosition(position: Position): Boolean {
            return this.position == position
        }

        override fun transposed(): Wall {
            return SingleBlock(position.transposed())
        }

        companion object {
            const val ORDINAL = 0
        }
    }

    class VerticalLine(val startPosition: Position, val endPosition: Position) : Wall {
        init {
            assert(startPosition.y < endPosition.y && startPosition.x == endPosition.x)
        }

        override fun containsPosition(position: Position): Boolean {
            return position.x == startPosition.x &&
                    position.y >= startPosition.y && position.y <= endPosition.y
        }

        override fun transposed(): Wall {
            return HorizontalLine(startPosition.transposed(), endPosition.transposed())
        }

        companion object {
            const val ORDINAL = 1
        }
    }

    class HorizontalLine(val startPosition: Position, val endPosition: Position) : Wall {
        init {
            assert(startPosition.x < endPosition.x && startPosition.y == endPosition.y)
        }

        override fun containsPosition(position: Position): Boolean {
            return position.y == startPosition.y &&
                    position.x >= startPosition.x && position.x <= endPosition.x
        }

        override fun transposed(): Wall {
            return VerticalLine(startPosition.transposed(), endPosition.transposed())
        }

        companion object {
            const val ORDINAL = 2
        }
    }
}
