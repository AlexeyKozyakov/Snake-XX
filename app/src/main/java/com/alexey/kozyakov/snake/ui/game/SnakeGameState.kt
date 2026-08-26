package com.alexey.kozyakov.snake.ui.game

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import com.alexey.kozyakov.snake.config.AI_FAIL_DECREASE_BY_LEVEL_RATIO
import com.alexey.kozyakov.snake.config.AI_FAIL_PROBABILITY_DEFAULT
import com.alexey.kozyakov.snake.config.APPLE_COUNT
import com.alexey.kozyakov.snake.config.BOOST_BY_BUTTON
import com.alexey.kozyakov.snake.config.BOOST_PER_LEVEL
import com.alexey.kozyakov.snake.config.INITIAL_SNAKE_LENGTH
import com.alexey.kozyakov.snake.config.LEVEL_GAIN_LENGTH_MULTIPLIER
import com.alexey.kozyakov.snake.config.MAX_TICK_INTERVAL_MS
import com.alexey.kozyakov.snake.config.OMNIVOROUS_TICKS
import com.alexey.kozyakov.snake.di.context
import com.alexey.kozyakov.snake.di.gameModelRepository
import com.alexey.kozyakov.snake.di.gameSettingsRepository
import com.alexey.kozyakov.snake.di.highScoreRepository
import com.alexey.kozyakov.snake.effects.haptic.SnakeGameHapticFeedbackPlayer
import com.alexey.kozyakov.snake.effects.sound.SnakeGameSoundEffectsPlayer
import com.alexey.kozyakov.snake.engine.SnakeGameEngine
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.model.SnakeGameModel
import com.alexey.kozyakov.snake.model.SnakeType
import com.alexey.kozyakov.snake.storage.SnakeGameHighScoreRepository
import com.alexey.kozyakov.snake.storage.model.SnakeGameModelRepository
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettingsRepository
import com.alexey.kozyakov.snake.ui.base.RetainedStateHolder
import com.alexey.kozyakov.snake.ui.base.asComposeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


class SnakeGameState(
    gridWidth: Int,
    gridHeight: Int,
    private val initialSnakeLength: Int,
    private val appleCount: Int,
    private val omnivorousTicks: Int,
    boostPerLevel: Double,
    boostByButton: Double,
    maxTickInterval: Duration,
    private val aiFailProbabilityDefault: Double,
    private val aiFailDecreaseByLevelRatio: Double,
    private val levelGainLengthMultiplier: Double,
    private val gameModelRepository: SnakeGameModelRepository,
    private val highScoreRepository: SnakeGameHighScoreRepository,
    gameSettingsRepository: SnakeGameSettingsRepository,
    context: Context
) : RetainedStateHolder() {
    private var engine = gameModelRepository.observe().value.let { restoredModel ->
        if (restoredModel != null) {
            SnakeGameEngine.restore(
                model = restoredModel,
                initialSnakeLength = initialSnakeLength,
                omnivorousTicks = omnivorousTicks,
                aiFailProbabilityDefault = aiFailProbabilityDefault,
                aiFailDecreaseByLevelRatio = aiFailDecreaseByLevelRatio,
                levelGainLengthMultiplier = levelGainLengthMultiplier
            )
        } else {
            SnakeGameEngine.create(
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
    }
    private val soundPlayer =
        SnakeGameSoundEffectsPlayer(context, stateHolderScope, gameSettingsRepository)
    private val hapticFeedbackPlayer =
        SnakeGameHapticFeedbackPlayer(context, stateHolderScope, gameSettingsRepository)

    var model by mutableStateOf(engine.model)
        private set
    var boost by mutableStateOf(false)
    var needsConfirmationToRun by mutableStateOf(false)
        private set
    var showLevel by mutableStateOf(true)
        private set
    private var resumed by mutableStateOf(true)
    val highScore by highScoreRepository
        .observe()
        .asComposeState(initialValue = 0)
    val gameIsOver by derivedStateOf { model.gameIsOver }
    val level by derivedStateOf { model.level }
    val tickInterval by derivedStateOf {
        maxTickInterval /
                boostPerLevel.pow(level) /
                (if (boost) boostByButton else 1.0)
    }
    val mainSnakeOmnivorousTicks by derivedStateOf {
        model.snakes
            .first { it.type == SnakeType.MAIN }
            .omnivorousTicksRemaining
    }
    val remainingLengthToGainLevel by derivedStateOf {
        model.remainingLengthToGainLevel
    }
    val score by derivedStateOf {
        model.score
    }
    val shouldRun by derivedStateOf {
        resumed && !gameIsOver && !needsConfirmationToRun
    }

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
                gridHeight = gridHeight,
                initialSnakeLength = initialSnakeLength,
                appleCount = appleCount,
                omnivorousTicks = omnivorousTicks,
                aiFailProbabilityDefault = aiFailProbabilityDefault,
                aiFailDecreaseByLevelRatio = aiFailDecreaseByLevelRatio,
                levelGainLengthMultiplier = levelGainLengthMultiplier
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
        if (!gameIsOver) {
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
            initialSnakeLength = INITIAL_SNAKE_LENGTH,
            appleCount = APPLE_COUNT,
            omnivorousTicks = OMNIVOROUS_TICKS,
            boostPerLevel = BOOST_PER_LEVEL,
            boostByButton = BOOST_BY_BUTTON,
            maxTickInterval = MAX_TICK_INTERVAL_MS.milliseconds,
            aiFailProbabilityDefault = AI_FAIL_PROBABILITY_DEFAULT,
            aiFailDecreaseByLevelRatio = AI_FAIL_DECREASE_BY_LEVEL_RATIO,
            levelGainLengthMultiplier = LEVEL_GAIN_LENGTH_MULTIPLIER,
            gameModelRepository = gameModelRepository,
            highScoreRepository = highScoreRepository,
            context = context,
            gameSettingsRepository = gameSettingsRepository
        )
    }
}
