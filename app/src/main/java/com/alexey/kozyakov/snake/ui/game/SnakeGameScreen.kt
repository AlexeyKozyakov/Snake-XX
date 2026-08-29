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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.config.MAIN_GRID_DIMENSION
import com.alexey.kozyakov.snake.model.Direction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val pressedButtonColor = Color(red = 211, green = 44, blue = 44, alpha = 255)
private val goldColor = Color(0xFFECCA32)

@Composable
fun SnakeGameScreen(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val (initialGridWidth, initialGridHeight) = with(density) {
            calculateGridDimensions(maxWidth.toPx(), maxHeight.toPx())
        }
        val state = retainSnakeGameState(
            gridWidth = initialGridWidth,
            gridHeight = initialGridHeight,
        )
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

        val coroutineScope = rememberCoroutineScope()
        val resizeDebounce = 50.milliseconds
        var resizeJob by remember { mutableStateOf<Job?>(null) }

        val renderer = rememberSnakeGameRenderer(state.snakeSkin)

        Canvas(
            Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    resizeJob?.cancel()
                    resizeJob = coroutineScope.launch {
                        val (gridWidth, gridHeight) =
                            calculateGridDimensions(
                                size.width.toFloat(),
                                size.height.toFloat()
                            )
                        delay(resizeDebounce)
                        state.resize(gridWidth, gridHeight)
                        resizeJob = null
                    }
                }
                .clickable(
                    enabled = true,
                    indication = null,
                    interactionSource = null
                ) {
                    state.restartFinishedGame()
                    state.confirmRunning()
                }
        ) {
            renderer.renderSnakeGame(state.model)
        }

        Row(
            Modifier
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
                text = state.balance.toString(),
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
            )
            Spacer(Modifier.size(4.dp))
            AnimatedVisibility(
                visible = state.addedBalanceVisible,
                enter = EnterTransition.None,
                exit = fadeOut(animationSpec = tween(durationMillis = 700)),
            ) {
                Text(
                    text = "+${state.addedBalanceAmount}",
                    color = goldColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                )
            }
        }

        Text(
            text = stringResource(R.string.remaining_length, state.remainingLengthToGainLevel),
            Modifier
                .align(Alignment.TopCenter)
                .padding(vertical = 8.dp)
                .statusBarsPadding(),
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = Color.White
        )

        Text(
            text = if (state.mainSnakeOmnivorousTicks > 0) {
                stringResource(R.string.omnivorous_ticks, state.mainSnakeOmnivorousTicks)
            } else {
                stringResource(R.string.score, state.score)
            },
            Modifier
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

        if (state.gameIsOver) {
            Column(Modifier.align(Alignment.Center)) {
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
                    text = stringResource(R.string.score, state.score),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.high_score, state.highScore),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        Column(Modifier.align(Alignment.Center)) {
            AnimatedVisibility(
                state.showLevel,
                Modifier.align(Alignment.CenterHorizontally),
                enter = EnterTransition.None,
                exit = fadeOut(animationSpec = tween(durationMillis = 700))
            ) {
                Text(
                    text = stringResource(R.string.level, state.level + 1),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                )
            }
            AnimatedVisibility(
                state.needsConfirmationToRun,
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(44.dp)
                .size(110.dp)
                .background(
                    (if (state.boost) pressedButtonColor else Color.LightGray).copy(alpha = 0.7f),
                    CircleShape
                )
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        state.boost = true
                        waitForUpOrCancellation()
                        state.boost = false
                    }
                }
        ) {
            Text(
                text = stringResource(R.string.boost_button),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .size(225.dp)
        ) {
            ControlButton(
                Icons.AutoMirrored.Default.KeyboardArrowLeft,
                Alignment.CenterStart,
            ) { state.setDirection(Direction.LEFT) }
            ControlButton(
                Icons.Default.KeyboardArrowUp,
                Alignment.TopCenter,
            ) { state.setDirection(Direction.UP) }
            ControlButton(
                Icons.AutoMirrored.Default.KeyboardArrowRight,
                Alignment.CenterEnd,
            ) { state.setDirection(Direction.RIGHT) }
            ControlButton(
                Icons.Default.KeyboardArrowDown,
                Alignment.BottomCenter,
            ) { state.setDirection(Direction.DOWN) }
        }
    }
}

@Composable
private fun BoxScope.ControlButton(
    icon: ImageVector,
    alignment: Alignment,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    Image(
        icon,
        contentDescription = null,
        Modifier
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

private fun calculateGridDimensions(widthPx: Float, heightPx: Float): Pair<Int, Int> {
    val cellSize = if (widthPx > heightPx) {
        widthPx / MAIN_GRID_DIMENSION
    } else {
        heightPx / MAIN_GRID_DIMENSION
    }
    val gridWidth = (widthPx / cellSize).toInt()
    val gridHeight = (heightPx / cellSize).toInt()
    return gridWidth to gridHeight
}
