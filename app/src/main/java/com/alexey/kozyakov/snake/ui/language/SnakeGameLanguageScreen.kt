package com.alexey.kozyakov.snake.ui.language

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguage
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuBackButton
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
        languages = state.languages,
        onBackClick = navigateBack,
        onLanguageClick = state::setLanguage
    )
}

@Composable
private fun SnakeGameLanguageScreen(
    modifier: Modifier = Modifier,
    languages: List<LanguageItem>,
    onBackClick: () -> Unit,
    onLanguageClick: (language: SnakeGameLanguage) -> Unit
) {
    Box(modifier.fillMaxSize()) {
        SnakeGameMenuContainer(modifier) {
            LanguagesHeader()
            Spacer(Modifier.size(32.dp))
            Languages(
                languages = languages,
                onLanguageClick = onLanguageClick
            )
        }
        SnakeGameMenuBackButton(onClick = onBackClick)
    }
}

@Composable
private fun FlowColumnScope.LanguagesHeader() {
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowColumnScope.Languages(
    languages: List<LanguageItem>,
    onLanguageClick: (SnakeGameLanguage) -> Unit
) {
    languages.forEach { (language, selected) ->
        SnakeGameMenuTextItem(
            Modifier
                .padding(bottom = 24.dp)
                .fillMaxColumnWidth(),
            primaryText = stringResource(language.nameResId),
            onClick = { onLanguageClick(language) },
            selected = selected
        )
    }
}

@Preview
@Composable
private fun Preview() {
    val selectedLanguageId = 1
    SnakeGameLanguageScreen(
        languages = SnakeGameLanguage.entries.map { language ->
            LanguageItem(
                language = language,
                selected = language.ordinal == selectedLanguageId
            )
        },
        onBackClick = { },
        onLanguageClick = { }
    )
}
