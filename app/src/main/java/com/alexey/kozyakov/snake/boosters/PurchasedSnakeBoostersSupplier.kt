package com.alexey.kozyakov.snake.boosters

import com.alexey.kozyakov.snake.engine.SnakeBoostersSupplier
import com.alexey.kozyakov.snake.storage.boosters.SnakeBooster
import com.alexey.kozyakov.snake.storage.boosters.PurchasedSnakeBoosterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PurchasedSnakeBoostersSupplier(
    private val coroutineScope: CoroutineScope,
    private val boosterRepository: PurchasedSnakeBoosterRepository,
    private val onBoosterConsumed: (booster: SnakeBooster, remaining: Int) -> Unit
) : SnakeBoostersSupplier {
    private var wallEatingBoostersCount = 0
    private var snakeEatingBoostersCount = 0

    init {
        coroutineScope.launch {
            val boosterCount = boosterRepository.observe().first()
            wallEatingBoostersCount = boosterCount[SnakeBooster.WALLS_EATING] ?: 0
            snakeEatingBoostersCount = boosterCount[SnakeBooster.SNAKE_EATING] ?: 0
        }
    }

    override fun consumeWallEatingBooster(): Boolean {
        return if (wallEatingBoostersCount > 0) {
            wallEatingBoostersCount--
            onBoosterConsumed(SnakeBooster.WALLS_EATING, wallEatingBoostersCount)
            saveBoosterCountToRepository()
            true
        } else {
            false
        }
    }

    override fun consumeSnakeEatingBooster(): Boolean {
        return if (snakeEatingBoostersCount > 0) {
            snakeEatingBoostersCount--
            onBoosterConsumed(SnakeBooster.SNAKE_EATING, snakeEatingBoostersCount)
            saveBoosterCountToRepository()
            true
        } else {
            false
        }
    }

    private fun saveBoosterCountToRepository() {
        coroutineScope.launch {
            boosterRepository.update { booster, _ ->
                when (booster) {
                    SnakeBooster.WALLS_EATING -> wallEatingBoostersCount
                    SnakeBooster.SNAKE_EATING -> snakeEatingBoostersCount
                }
            }
        }
    }
}
