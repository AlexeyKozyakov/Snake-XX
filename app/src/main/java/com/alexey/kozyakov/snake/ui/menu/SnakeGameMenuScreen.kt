package com.alexey.kozyakov.snake.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.storage.skins.SnakeSkin
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuContainer
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuTextItem

@Composable
fun SnakeGameMenuScreen(
    modifier: Modifier = Modifier,
    navigateToGameScreen: () -> Unit,
    navigateToSettingsScreen: () -> Unit,
    navigateToShopScreen: () -> Unit
) {
    val state = retainSnakeGameMenuState()
    SnakeGameMenuScreen(
        modifier = modifier,
        continueButtonEnabled = state.canContinueGame,
        level = state.level,
        skin = state.skin,
        onNewGameClick = {
            state.resetGame()
            navigateToGameScreen()
        },
        onContinueClick = navigateToGameScreen,
        onSettingsClick = navigateToSettingsScreen,
        onShopClick = navigateToShopScreen
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SnakeGameMenuScreen(
    modifier: Modifier = Modifier,
    continueButtonEnabled: Boolean,
    level: Int,
    skin: SnakeSkin,
    onNewGameClick: () -> Unit,
    onContinueClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShopClick: () -> Unit
) {
    SnakeGameMenuContainer(modifier) {
        Text(
            text = stringResource(R.string.app_name),
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 38.dp)
        )
        Row(
            Modifier.padding(bottom = 32.dp)
        ) {
            Image(
                painter = painterResource(skin.headResId),
                contentDescription = null,
                Modifier
                    .size(60.dp)
                    .scale(1.2f)
            )
            Image(
                painter = painterResource(skin.bodyResId),
                contentDescription = null,
                Modifier
                    .size(60.dp)
                    .scale(1.2f)
            )
            Image(
                painter = painterResource(skin.bodyResId),
                contentDescription = null,
                Modifier
                    .size(60.dp)
                    .scale(1.2f)
            )
        }
        Row(Modifier.padding(bottom = 42.dp)) {
            Image(
                painter = painterResource(R.drawable.snake_yellow_xx),
                contentDescription = null,
                Modifier
                    .size(60.dp)
                    .scale(1.2f)
            )
            Image(
                painter = painterResource(R.drawable.snake_yellow_blob),
                contentDescription = null,
                Modifier
                    .size(60.dp)
                    .scale(1.2f)
            )
            Image(
                painter = painterResource(R.drawable.snake_yellow_blob),
                contentDescription = null,
                Modifier
                    .size(60.dp)
                    .scale(1.2f)
            )
        }
        SnakeGameMenuTextItem(
            Modifier.padding(bottom = 24.dp).fillMaxColumnWidth(),
            primaryText = stringResource(R.string.new_game),
            primaryTextSize = 42.sp,
            onClick = onNewGameClick
        )
        SnakeGameMenuTextItem(
            Modifier.fillMaxColumnWidth(),
            primaryText = stringResource(R.string.continue_game),
            secondaryText = if (continueButtonEnabled) {
                stringResource(R.string.level_menu_item, level)
            } else {
                null
            },
            primaryTextSize = 42.sp,
            enabled = continueButtonEnabled,
            onClick = onContinueClick
        )
        Spacer(Modifier.size(24.dp))
        SnakeGameMenuTextItem(
            Modifier.padding(bottom = 24.dp).fillMaxColumnWidth(),
            primaryText = stringResource(R.string.shop_menu_item),
            primaryTextSize = 42.sp,
            enabled = true,
            onClick = onShopClick
        )
        SnakeGameMenuTextItem(
            Modifier.fillMaxColumnWidth(),
            primaryText = stringResource(R.string.open_settings),
            primaryTextSize = 42.sp,
            onClick = onSettingsClick
        )
    }
}

@Preview
@Composable
private fun Preview() {
    SnakeGameMenuScreen(
        continueButtonEnabled = true,
        level = 1,
        skin = SnakeSkin.SLIME,
        onNewGameClick = { },
        onContinueClick = { },
        onSettingsClick = { },
        onShopClick = { }
    )
}
