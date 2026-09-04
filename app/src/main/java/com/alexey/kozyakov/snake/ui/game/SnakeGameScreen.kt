package com.alexey.kozyakov.snake.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.config.MIN_MAIN_GRID_DIMENSION
import com.alexey.kozyakov.snake.model.Direction
import kotlin.math.max

private val pressedButtonColor = Color(0xFFD32C2C)
private val goldColor = Color(0xFFECCA32)

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
            onClick = {
                state.restartFinishedGame()
                state.confirmRunning()
            },
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
            GameOver(state.score, state.highScore)
        }

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
        Text(
            text = balance.toString(),
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
        )
        Spacer(Modifier.size(4.dp))
        AnimatedVisibility(
            visible = addedBalanceVisible,
            enter = EnterTransition.None,
            exit = fadeOut(animationSpec = tween(durationMillis = 700)),
        ) {
            Text(
                text = "+$addedBalanceAmount",
                color = goldColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun BoxScope.RemainingLength(
    length: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.remaining_length, length),
        modifier
            .align(Alignment.TopCenter)
            .padding(vertical = 8.dp)
            .statusBarsPadding(),
        fontFamily = FontFamily.Monospace,
        fontSize = 16.sp,
        color = Color.White
    )
}

@Composable
private fun BoxScope.Score(
    score: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.score, score),
        modifier
            .align(Alignment.TopEnd)
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            )
            .statusBarsPadding(),
        color = Color.White,
        fontFamily = FontFamily.Monospace,
        fontSize = 16.sp,
    )
}

@Composable
private fun BoxScope.GameOver(
    score: Int,
    highScore: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier.align(Alignment.Center)) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.game_over),
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.score, score),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.high_score, highScore),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
        )
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
        AnimatedVisibility(
            showLevel,
            Modifier.align(Alignment.CenterHorizontally),
            enter = EnterTransition.None,
            exit = fadeOut(animationSpec = tween(durationMillis = 700))
        ) {
            Text(
                text = stringResource(R.string.level, level + 1),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
            )
        }
        AnimatedVisibility(
            showConfirmation,
            Modifier.align(Alignment.CenterHorizontally),
            enter = EnterTransition.None,
            exit = fadeOut(animationSpec = tween(durationMillis = 700))
        ) {
            Text(
                text = stringResource(R.string.confirmation),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Monospace
                ),
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
        Text(
            text = stringResource(R.string.boost_button),
            color = Color.White,
            modifier = Modifier.align(Alignment.Center),
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

private fun calculateGridDimensions(widthPx: Int, heightPx: Int): Pair<Int, Int> {
    val gcd = widthPx.toBigInteger().gcd(heightPx.toBigInteger()).toInt()
    val mainDimensionSizePx = max(widthPx, heightPx)
    var cellSize = gcd
    while (mainDimensionSizePx / cellSize < MIN_MAIN_GRID_DIMENSION && cellSize % 2 == 0) {
        cellSize /= 2
    }
    val gridWidth = widthPx / cellSize
    val gridHeight = heightPx / cellSize
    return gridWidth to gridHeight
}
