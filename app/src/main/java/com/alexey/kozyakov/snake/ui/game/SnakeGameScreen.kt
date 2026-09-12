package com.alexey.kozyakov.snake.ui.game

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.model.Direction
import com.alexey.kozyakov.snake.storage.boosters.SnakeBooster
import com.alexey.kozyakov.snake.ui.components.MonospaceText
import com.alexey.kozyakov.snake.ui.components.SnakeGameActionButton
import com.alexey.kozyakov.snake.ui.components.grassColor

private val pressedButtonColor = Color(0xFFD32C2C)
private val addedBalanceColor = Color(0xFFECCA32)
private val borderColor = Color(0xFFFFE000)
private val secondBorderColor = Color(0xFFFFA040)

private const val FADE_OUT_ANIMATION_DURATION = 700

@Composable
fun SnakeGameScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {

        val state = retainSnakeGameState()
        val renderer = rememberSnakeGameRenderer(state.snakeSkin)

        LaunchedEffect(state.shouldRun) {
            if (state.shouldRun) {
                state.runGame()
            }
        }

        LifecycleResumeEffect(Unit) {
            state.resume()
            onPauseOrDispose {
                state.pause()
            }
        }

        GameCanvas(
            onSizeChanged = { size ->
                val (gridWidth, gridHeight) =
                    calculateGridDimensions(size.width, size.height)
                state.resize(gridWidth, gridHeight)
            },
            onClick = state::confirmRunning,
            onDraw = {
                renderer.renderSnakeGame(state.model)
            }
        )

        BalanceDisplay(
            balance = state.balance,
            addedBalanceAmount = state.addedBalanceAmount,
            addedBalanceVisible = state.addedBalanceVisible
        )

        RemainingLength(state.remainingLengthToGainLevel)

        Score(state.score)

        if (state.gameIsOver) {
            GameOver(
                score = state.score,
                highScore = state.highScore,
                canContinue = state.canContinue,
                continuePrice = state.continuePrice,
                onRestartClick = state::restartFinishedGame,
                onContinueClick = state::continueFinishedGame
            )
        }

        ConsumedBooster(
            visible = state.consumedBoosterVisible,
            consumedBooster = state.consumedBooster,
        )

        LevelAndConfirmation(
            showLevel = state.showLevel,
            level = state.level,
            showConfirmation = state.needsConfirmationToRun
        )

        BoostButton(
            boostEnabled = state.boost,
            onPressed = { pressed -> state.boost = pressed }
        )

        DirectionControls(onClick = state::setDirection)
    }
}

@Composable
private fun GameCanvas(
    onSizeChanged: (size: IntSize) -> Unit,
    onClick: () -> Unit,
    onDraw: DrawScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier
            .fillMaxSize()
            .onSizeChanged(onSizeChanged)
            .clickable(
                enabled = true,
                indication = null,
                interactionSource = null,
                onClick = onClick
            ),
        onDraw = onDraw
    )
}

@Composable
private fun BoxScope.BalanceDisplay(
    balance: Int,
    addedBalanceAmount: Int,
    addedBalanceVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .align(Alignment.TopStart)
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            )
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.coin),
            contentDescription = null,
            Modifier.size(24.dp)
        )
        Spacer(Modifier.size(8.dp))
        MonospaceText(
            text = balance.toString(),
            fontSize = 16.sp
        )
        Spacer(Modifier.size(4.dp))
        FadeOut(visible = addedBalanceVisible) {
            MonospaceText(
                text = "+$addedBalanceAmount",
                color = addedBalanceColor,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun BoxScope.RemainingLength(
    length: Int,
    modifier: Modifier = Modifier
) {
    MonospaceText(
        text = stringResource(R.string.remaining_length, length),
        fontSize = 16.sp,
        modifier = modifier
            .align(Alignment.TopCenter)
            .padding(vertical = 8.dp)
            .statusBarsPadding(),
    )
}

@Composable
private fun BoxScope.Score(
    score: Int,
    modifier: Modifier = Modifier
) {
    MonospaceText(
        text = stringResource(R.string.score, score),
        fontSize = 16.sp,
        modifier = modifier
            .align(Alignment.TopEnd)
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            )
            .statusBarsPadding(),
    )
}

@Composable
private fun BoxScope.GameOver(
    score: Int,
    highScore: Int,
    canContinue: Boolean,
    continuePrice: Int,
    onRestartClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .align(Alignment.Center)
            .background(
                color = grassColor,
                shape = RoundedCornerShape(36.dp)
            )
            .border(
                width = 1.dp,
                color = secondBorderColor,
                shape = RoundedCornerShape(36.dp)
            )
            .border(
                width = 4.dp,
                color = borderColor,
                shape = RoundedCornerShape(36.dp)
            )
            .padding(16.dp)
    ) {
        MonospaceText(
            text = stringResource(R.string.game_over),
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.size(8.dp))
        MonospaceText(
            text = stringResource(R.string.score, score),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.size(8.dp))
        MonospaceText(
            text = stringResource(R.string.high_score, highScore),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.size(8.dp))
        val isPortrait =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(fraction = if (isPortrait) 0.8f else 0.4f),
        ) {
            SnakeGameActionButton(
                Modifier.padding(12.dp),
                enabled = canContinue,
                onClick = onContinueClick
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    MonospaceText(
                        text = stringResource(R.string.continue_button, continuePrice)
                    )
                    Spacer(Modifier.size(8.dp))
                    Image(
                        painter = painterResource(R.drawable.coin),
                        contentDescription = null,
                        Modifier
                            .size(22.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
            SnakeGameActionButton(
                Modifier.padding(12.dp),
                enabled = true,
                onClick = onRestartClick
            ) {
                MonospaceText(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    text = stringResource(R.string.restart_button),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BoxScope.ConsumedBooster(
    visible: Boolean,
    consumedBooster: ConsumedBooster,
    modifier: Modifier = Modifier
) {
    val wallsEatingBitmap = ImageBitmap.imageResource(R.drawable.booster_eat_walls)
    val snakeEatingBitmap = ImageBitmap.imageResource(R.drawable.booster_eat_snake)
    FadeOut(
        visible = visible,
        modifier = modifier
            .align(Alignment.Center)
            .alpha(0.8f)
    ) {
        val (booster, remaining) = consumedBooster
        Column(
            Modifier
                .align(Alignment.Center)
                .background(
                    color = grassColor,
                    shape = RoundedCornerShape(36.dp)
                )
                .border(
                    width = 1.dp,
                    color = secondBorderColor,
                    shape = RoundedCornerShape(36.dp)
                )
                .border(
                    width = 4.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(36.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                bitmap = when (booster) {
                    SnakeBooster.WALLS_EATING -> wallsEatingBitmap
                    SnakeBooster.SNAKE_EATING -> snakeEatingBitmap
                },
                contentDescription = null,
                Modifier.size(150.dp)
            )
            MonospaceText(
                text = stringResource(R.string.booster_remaining_count, remaining),
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BoxScope.LevelAndConfirmation(
    showLevel: Boolean,
    level: Int,
    showConfirmation: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier.align(Alignment.Center)) {
        FadeOut(
            visible = showLevel,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            MonospaceText(
                text = stringResource(R.string.level, level + 1),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        FadeOut(
            visible = showConfirmation,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            MonospaceText(
                text = stringResource(R.string.confirmation),
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.BoostButton(
    boostEnabled: Boolean,
    onPressed: (pressed: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .align(Alignment.BottomStart)
            .navigationBarsPadding()
            .padding(44.dp)
            .size(110.dp)
            .background(
                (if (boostEnabled) pressedButtonColor else Color.LightGray).copy(alpha = 0.7f),
                CircleShape
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onPressed(true)
                    waitForUpOrCancellation()
                    onPressed(false)
                }
            }
    ) {
        MonospaceText(
            text = stringResource(R.string.boost_button),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun BoxScope.DirectionControls(
    onClick: (direction: Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .size(225.dp)
    ) {
        DirectionButton(
            Icons.AutoMirrored.Default.KeyboardArrowLeft,
            Alignment.CenterStart,
            onClick = { onClick(Direction.LEFT) }
        )
        DirectionButton(
            Icons.Default.KeyboardArrowUp,
            Alignment.TopCenter,
            onClick = { onClick(Direction.UP) }
        )
        DirectionButton(
            Icons.AutoMirrored.Default.KeyboardArrowRight,
            Alignment.CenterEnd,
            onClick = { onClick(Direction.RIGHT) }
        )
        DirectionButton(
            Icons.Default.KeyboardArrowDown,
            Alignment.BottomCenter,
            onClick = { onClick(Direction.DOWN) }
        )
    }
}

@Composable
private fun BoxScope.DirectionButton(
    icon: ImageVector,
    alignment: Alignment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    Image(
        icon,
        contentDescription = null,
        modifier
            .align(alignment)
            .alpha(0.8f)
            .size(95.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    onClick()
                    waitForUpOrCancellation()
                    pressed = false
                }
            },
        colorFilter = ColorFilter.tint(if (pressed) pressedButtonColor else Color.White)
    )
}

@Composable
private fun FadeOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible,
        modifier,
        enter = EnterTransition.None,
        exit = fadeOut(animationSpec = tween(durationMillis = FADE_OUT_ANIMATION_DURATION)),
        content = content
    )
}
