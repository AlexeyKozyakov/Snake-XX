package com.alexey.kozyakov.snake.ui.game

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import com.alexey.kozyakov.snake.balance.SnakeGameBalanceUpdater
import com.alexey.kozyakov.snake.config.BOOST_BY_BUTTON
import com.alexey.kozyakov.snake.config.BOOST_PER_LEVEL
import com.alexey.kozyakov.snake.config.MAX_TICK_INTERVAL_MS
import com.alexey.kozyakov.snake.di.balanceRepository
import com.alexey.kozyakov.snake.di.context
import com.alexey.kozyakov.snake.di.gameModelRepository
import com.alexey.kozyakov.snake.di.gameSettingsRepository
import com.alexey.kozyakov.snake.di.highScoreRepository
import com.alexey.kozyakov.snake.di.snakeSkinRepository
import com.alexey.kozyakov.snake.di.upgradeRepository
import com.alexey.kozyakov.snake.effects.haptic.SnakeGameHapticFeedbackPlayer
import com.alexey.kozyakov.snake.effects.sound.SnakeGameSoundEffectsPlayer
import com.alexey.kozyakov.snake.engine.SnakeGameEngine
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.model.SnakeGameModel
import com.alexey.kozyakov.snake.storage.balance.SnakeGameBalanceRepository
import com.alexey.kozyakov.snake.storage.highscore.SnakeGameHighScoreRepository
import com.alexey.kozyakov.snake.storage.model.SnakeGameModelRepository
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettingsRepository
import com.alexey.kozyakov.snake.storage.skins.SnakeSkin
import com.alexey.kozyakov.snake.storage.skins.SnakeSkinRepository
import com.alexey.kozyakov.snake.storage.upgrade.SnakeUpgradeRepository
import com.alexey.kozyakov.snake.ui.base.RetainedStateHolder
import com.alexey.kozyakov.snake.ui.base.asComposeState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds


class SnakeGameState(
    gridWidth: Int,
    gridHeight: Int,
    private val gameModelRepository: SnakeGameModelRepository,
    private val highScoreRepository: SnakeGameHighScoreRepository,
    gameSettingsRepository: SnakeGameSettingsRepository,
    snakeSkinRepository: SnakeSkinRepository,
    balanceRepository: SnakeGameBalanceRepository,
    upgradeRepository: SnakeUpgradeRepository,
    context: Context
) : RetainedStateHolder() {
    private var engine = gameModelRepository.observe().value.let { restoredModel ->
        if (restoredModel != null) {
            SnakeGameEngine.restore(restoredModel)
        } else {
            SnakeGameEngine.create(
                gridWidth = gridWidth,
                gridHeight = gridHeight
            )
        }
    }
    private val maxTickInterval = MAX_TICK_INTERVAL_MS.milliseconds
    private val soundPlayer =
        SnakeGameSoundEffectsPlayer(context, stateHolderScope, gameSettingsRepository)
    private val hapticFeedbackPlayer =
        SnakeGameHapticFeedbackPlayer(context, stateHolderScope, gameSettingsRepository)
    private val balanceUpdater =
        SnakeGameBalanceUpdater(stateHolderScope, balanceRepository, upgradeRepository)

    private val tickInterval by derivedStateOf {
        maxTickInterval /
                BOOST_PER_LEVEL.pow(level) /
                (if (boost) BOOST_BY_BUTTON else 1.0)
    }
    var model by mutableStateOf(engine.model)
        private set
    var boost by mutableStateOf(false)
    var needsConfirmationToRun by mutableStateOf(false)
        private set
    var showLevel by mutableStateOf(true)
        private set
    var addedBalanceAmount by mutableIntStateOf(0)
    var addedBalanceVisible by mutableStateOf(false)
    private var balanceHideJob: Job? = null
    private var resumed by mutableStateOf(true)
    val highScore by highScoreRepository
        .observe()
        .asComposeState(initialValue = 0)
    val gameIsOver by derivedStateOf { model.gameIsOver }
    val level by derivedStateOf { model.level }
    val remainingLengthToGainLevel by derivedStateOf {
        model.remainingLengthToGainLevel
    }
    val score by derivedStateOf {
        model.score
    }
    val shouldRun by derivedStateOf {
        resumed && !gameIsOver && !needsConfirmationToRun
    }
    val snakeSkin by snakeSkinRepository
        .observe()
        .asComposeState(initialValue = SnakeSkin.DEFAULT)
    val balance by balanceRepository
        .observe()
        .asComposeState(initialValue = 0)

    init {
        showLevel()
    }

    suspend fun runGame() {
        while (true) {
            delay(tickInterval)
            step()
        }
    }

    fun restartFinishedGame() {
        if (engine.restartFinishedGame()) {
            updateState()
        }
    }

    fun setDirection(direction: Direction) {
        if (shouldRun) {
            engine.setDirection(newDirection = direction)
        }
    }

    fun resize(gridWidth: Int, gridHeight: Int) {
        val model = engine.model
        if (gridHeight == model.gridHeight && gridWidth == model.gridWidth) {
            return
        }
        if (gridHeight == model.gridWidth && gridWidth == model.gridHeight) {
            engine.transposeGrid()
        } else {
            engine = SnakeGameEngine.create(
                gridWidth = gridWidth,
                gridHeight = gridHeight
            )
        }
        updateState()
    }

    fun confirmRunning() {
        if (needsConfirmationToRun) {
            needsConfirmationToRun = false
            showLevel = false
        }
    }

    fun pause() {
        resumed = false
        saveGame(model)
    }

    fun resume() {
        resumed = true
    }

    override fun dispose() {
        super.dispose()
        soundPlayer.dispose()
    }

    private fun saveGame(model: SnakeGameModel) {
        if (!model.gameIsOver) {
            stateHolderScope.launch {
                gameModelRepository.save(model)
            }
        }
    }

    private fun step() {
        engine.step()
        updateState()
        soundPlayer.playSoundEffects(engine.events)
        hapticFeedbackPlayer.playHapticFeedback(engine.events)
        val balanceDiff = balanceUpdater.update(model, engine.events)
        if (balanceDiff > 0) {
            showAddedBalance(balanceDiff)
        }
    }

    private fun updateState() {
        val newModel = engine.model
        when {
            newModel.gameIsOver && !model.gameIsOver -> {
                stateHolderScope.launch {
                    if (newModel.score > highScore) {
                        highScoreRepository.save(newModel.score)
                    }
                    gameModelRepository.reset()
                }
            }

            newModel.level > model.level -> {
                saveGame(newModel)
                needsConfirmationToRun = true
                showLevel()
            }

            !newModel.gameIsOver && model.gameIsOver -> {
                showLevel()
            }
        }
        model = newModel
    }

    private fun showLevel() {
        showLevel = true
        if (!needsConfirmationToRun) {
            stateHolderScope.launch {
                delay(300.milliseconds)
                showLevel = false
            }
        }
    }

    private fun showAddedBalance(amount: Int) {
        balanceHideJob?.cancel()
        addedBalanceAmount = amount
        addedBalanceVisible = true
        balanceHideJob = stateHolderScope.launch {
            delay(300.milliseconds)
            addedBalanceVisible = false
            balanceHideJob = null
        }
    }
}

@Composable
fun retainSnakeGameState(
    gridWidth: Int,
    gridHeight: Int,
): SnakeGameState {
    return retain {
        SnakeGameState(
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            gameModelRepository = gameModelRepository,
            highScoreRepository = highScoreRepository,
            snakeSkinRepository = snakeSkinRepository,
            gameSettingsRepository = gameSettingsRepository,
            balanceRepository = balanceRepository,
            upgradeRepository = upgradeRepository,
            context = context
        )
    }
}
