package com.alexey.kozyakov.snake.ui.language

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguage
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuContainer
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuTextItem

@Composable
fun SnakeGameLanguageScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit
) {
    val state = retainSnakeGameLanguageState()
    SnakeGameLanguageScreen(
        modifier,
        language = state.language,
        onBackClick = navigateBack,
        onLanguageClick = state::setLanguage
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SnakeGameLanguageScreen(
    modifier: Modifier = Modifier,
    language: SnakeGameLanguage,
    onBackClick: () -> Unit,
    onLanguageClick: (language: SnakeGameLanguage) -> Unit
) {
    Box(modifier.fillMaxSize()) {
        SnakeGameMenuContainer(modifier) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 38.dp),
                text = stringResource(R.string.language_settings_screen),
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Row {
                Image(
                    painter = painterResource(R.drawable.snake_yellow_xx),
                    contentDescription = null,
                    Modifier
                        .padding(end = 18.dp)
                        .size(64.dp),
                )
                Image(
                    painter = painterResource(R.drawable.apple_alt),
                    contentDescription = null,
                    Modifier
                        .padding(end = 18.dp)
                        .size(64.dp),
                )
                Image(
                    painter = painterResource(R.drawable.apple_green),
                    contentDescription = null,
                    Modifier.size(64.dp),
                )
            }
            Spacer(Modifier.size(32.dp))
            SnakeGameMenuTextItem(
                Modifier
                    .padding(bottom = 24.dp)
                    .fillMaxColumnWidth(),
                primaryText = stringResource(R.string.language_system),
                onClick = { onLanguageClick(SnakeGameLanguage.SYSTEM) },
                selected = language == SnakeGameLanguage.SYSTEM
            )
            SnakeGameMenuTextItem(
                Modifier
                    .padding(bottom = 24.dp)
                    .fillMaxColumnWidth(),
                primaryText = stringResource(R.string.language_english),
                onClick = { onLanguageClick(SnakeGameLanguage.ENGLISH) },
                selected = language == SnakeGameLanguage.ENGLISH
            )
            SnakeGameMenuTextItem(
                Modifier.fillMaxColumnWidth(),
                primaryText = stringResource(R.string.language_russian),
                onClick = { onLanguageClick(SnakeGameLanguage.RUSSIAN) },
                selected = language == SnakeGameLanguage.RUSSIAN
            )
        }
        Image(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier
                .clickable(enabled = true, onClick = onBackClick)
                .padding(10.dp)
                .statusBarsPadding()
                .size(56.dp)
                .align(Alignment.TopStart)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    SnakeGameLanguageScreen(
        language = SnakeGameLanguage.ENGLISH,
        onBackClick = { },
        onLanguageClick = { }
    )
}
