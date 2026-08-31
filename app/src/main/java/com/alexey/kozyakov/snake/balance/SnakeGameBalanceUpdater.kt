package com.alexey.kozyakov.snake.balance

import com.alexey.kozyakov.snake.config.COIN_COST
import com.alexey.kozyakov.snake.config.DIAMOND_COST
import com.alexey.kozyakov.snake.config.GOLDEN_APPLE_COST
import com.alexey.kozyakov.snake.config.INITIAL_LEVEL_COST
import com.alexey.kozyakov.snake.config.LEVEL_COST_MULTIPLIER
import com.alexey.kozyakov.snake.model.SnakeGameEvent
import com.alexey.kozyakov.snake.model.SnakeGameModel
import com.alexey.kozyakov.snake.storage.balance.SnakeGameBalanceRepository
import com.alexey.kozyakov.snake.storage.upgrade.SnakeUpgrade
import com.alexey.kozyakov.snake.storage.upgrade.SnakeUpgradeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnakeGameBalanceUpdater(
    private val coroutineScope: CoroutineScope,
    private val balanceRepository: SnakeGameBalanceRepository,
    private val upgradeRepository: SnakeUpgradeRepository
) {
    private var isCoinsForGoldenApplesEnabled = false
    private var isCoinsForLevelsEnabled = false

    init {
        coroutineScope.launch {
            upgradeRepository.observe().collect { upgrades ->
                isCoinsForGoldenApplesEnabled = SnakeUpgrade.COINS_FOR_GOLDEN_APPLES in upgrades
                isCoinsForLevelsEnabled = SnakeUpgrade.COINS_FOR_LEVELS in upgrades
            }
        }
    }

    fun update(
        model: SnakeGameModel,
        events: List<SnakeGameEvent>
    ): Int {
        val amount = events.sumOf { event ->
            when (event) {
                SnakeGameEvent.COIN_PICKED -> COIN_COST

                SnakeGameEvent.DIAMOND_PICKED -> DIAMOND_COST

                SnakeGameEvent.GOLDEN_APPLE_EATEN -> {
                    if (isCoinsForGoldenApplesEnabled) {
                        GOLDEN_APPLE_COST
                    } else {
                        0
                    }
                }

                SnakeGameEvent.LEVEL_GAINED -> {
                    if (isCoinsForLevelsEnabled) {
                        INITIAL_LEVEL_COST * Math.powExact(LEVEL_COST_MULTIPLIER, model.level - 1)
                    } else {
                        0
                    }
                }

                else -> 0
            }
        }
        if (amount > 0) {
            coroutineScope.launch {
                balanceRepository.update { balance -> balance + amount }
            }
        }
        return amount
    }
}
