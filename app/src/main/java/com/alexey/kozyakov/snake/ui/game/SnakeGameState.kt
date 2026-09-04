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
import com.alexey.kozyakov.snake.boosters.PurchasedSnakeBoostersSupplier
import com.alexey.kozyakov.snake.config.BOOST_BY_BUTTON
import com.alexey.kozyakov.snake.config.BOOST_PER_LEVEL
import com.alexey.kozyakov.snake.config.MAX_TICK_INTERVAL_MS
import com.alexey.kozyakov.snake.di.balanceRepository
import com.alexey.kozyakov.snake.di.context
import com.alexey.kozyakov.snake.di.gameModelRepository
import com.alexey.kozyakov.snake.di.gameSettingsRepository
import com.alexey.kozyakov.snake.di.highScoreRepository
import com.alexey.kozyakov.snake.di.purchasedBoosterRepository
import com.alexey.kozyakov.snake.di.snakeSkinRepository
import com.alexey.kozyakov.snake.di.upgradeRepository
import com.alexey.kozyakov.snake.effects.haptic.SnakeGameHapticFeedbackPlayer
import com.alexey.kozyakov.snake.effects.sound.SnakeGameSoundEffectsPlayer
import com.alexey.kozyakov.snake.engine.SnakeGameEngine
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.model.SnakeGameModel
import com.alexey.kozyakov.snake.storage.balance.SnakeGameBalanceRepository
import com.alexey.kozyakov.snake.storage.boosters.PurchasedSnakeBoosterRepository
import com.alexey.kozyakov.snake.storage.boosters.SnakeBooster
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
    private val gameModelRepository: SnakeGameModelRepository,
    private val highScoreRepository: SnakeGameHighScoreRepository,
    gameSettingsRepository: SnakeGameSettingsRepository,
    snakeSkinRepository: SnakeSkinRepository,
    balanceRepository: SnakeGameBalanceRepository,
    upgradeRepository: SnakeUpgradeRepository,
    purchasedBoosterRepository: PurchasedSnakeBoosterRepository,
    context: Context
) : RetainedStateHolder() {
    private val boostersSupplier = PurchasedSnakeBoostersSupplier(
        coroutineScope = stateHolderScope,
        boosterRepository = purchasedBoosterRepository,
        onBoosterConsumed = ::showConsumedBooster
    )
    private var sizeInitialized = false
    private var engine = gameModelRepository.observe().value?.let { restoredModel ->
        sizeInitialized = true
        SnakeGameEngine.restore(
            model = restoredModel,
            boostersSupplier = boostersSupplier
        )
    } ?: SnakeGameEngine.empty()

    private val soundPlayer =
        SnakeGameSoundEffectsPlayer(context, stateHolderScope, gameSettingsRepository)
    private val hapticFeedbackPlayer =
        SnakeGameHapticFeedbackPlayer(context, stateHolderScope, gameSettingsRepository)
    private val balanceUpdater =
        SnakeGameBalanceUpdater(stateHolderScope, balanceRepository, upgradeRepository)

    private val maxTickInterval = MAX_TICK_INTERVAL_MS.milliseconds
    private val tickInterval by derivedStateOf {
        maxTickInterval /
                BOOST_PER_LEVEL.pow(level) /
                (if (boost) BOOST_BY_BUTTON else 1.0)
    }

    private val resizeDebounce = 50.milliseconds
    private var balanceHideJob: Job? = null
    private var resizeJob: Job? = null
    private var resumed by mutableStateOf(true)

    var model by mutableStateOf(engine.model)
        private set
    var boost by mutableStateOf(false)
    var needsConfirmationToRun by mutableStateOf(false)
        private set
    var showLevel by mutableStateOf(true)
        private set
    var addedBalanceAmount by mutableIntStateOf(0)
    var addedBalanceVisible by mutableStateOf(false)

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
        if (!sizeInitialized) {
            doResize(gridWidth, gridHeight)
            sizeInitialized = true
            return
        }
        resizeJob?.cancel()
        resizeJob = stateHolderScope.launch {
            delay(resizeDebounce)
            doResize(gridWidth, gridHeight)
            resizeJob = null
        }
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

    private fun doResize(gridWidth: Int, gridHeight: Int) {
        val model = engine.model
        if (gridHeight == model.gridHeight && gridWidth == model.gridWidth) {
            return
        }
        if (gridHeight == model.gridWidth && gridWidth == model.gridHeight) {
            engine.transposeGrid()
        } else {
            engine = SnakeGameEngine.create(
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                boostersSupplier = boostersSupplier
            )
        }
        updateState()
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

    // TODO(boosters):
    //  1. Добавить/поменять картинки для бустеров
    //  2. Показывать картинку и оставшееся количество при использовании
    //  3. Добавить товары бустеров с описанием в магазин
    //  4. Доработать логику магазина для покупки бустеров
    // TODO(справка):
    //  1. Добавить экран со справкой по приложению с описанием цели игры и бонусов
    // TODO(продолжение)
    //  1. Продолжать игру после смерти за монетки
    private fun showConsumedBooster(booster: SnakeBooster, remaining: Int) {
        TODO("$booster $remaining")
    }
}

@Composable
fun retainSnakeGameState(): SnakeGameState {
    return retain {
        SnakeGameState(
            gameModelRepository = gameModelRepository,
            highScoreRepository = highScoreRepository,
            snakeSkinRepository = snakeSkinRepository,
            gameSettingsRepository = gameSettingsRepository,
            balanceRepository = balanceRepository,
            upgradeRepository = upgradeRepository,
            purchasedBoosterRepository = purchasedBoosterRepository,
            context = context
        )
    }
}
