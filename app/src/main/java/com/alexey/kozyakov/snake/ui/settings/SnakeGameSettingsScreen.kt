package com.alexey.kozyakov.snake.ui.settings

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
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettings
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuContainer
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuTextItem

@Composable
fun SnakeGameSettingsScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    navigateToLanguageSettings: () -> Unit
) {
    val state = retainSnakeGameSettingsState()
    SnakeGameSettingsScreen(
        modifier,
        settings = state.settings,
        language = state.language,
        onBackClick = navigateBack,
        onVibrationSettingClick = state::toggleVibration,
        onMusicSettingClick = state::toggleMusic,
        onSoundEffectsSettingClick = state::toggleSoundEffects,
        onLanguageSettingClick = navigateToLanguageSettings
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SnakeGameSettingsScreen(
    modifier: Modifier = Modifier,
    settings: SnakeGameSettings,
    language: SnakeGameLanguage,
    onBackClick: () -> Unit,
    onVibrationSettingClick: () -> Unit,
    onMusicSettingClick: () -> Unit,
    onSoundEffectsSettingClick: () -> Unit,
    onLanguageSettingClick: () -> Unit
) {
    Box(modifier.fillMaxSize()) {
        SnakeGameMenuContainer(Modifier.align(Alignment.Center)) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 38.dp),
                text = stringResource(R.string.settings_screen),
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Row {
                Image(
                    painter = painterResource(R.drawable.snake_green_xx),
                    contentDescription = null,
                    Modifier
                        .padding(end = 18.dp)
                        .size(64.dp),
                )
                Image(
                    painter = painterResource(R.drawable.wall_block_0),
                    contentDescription = null,
                    Modifier
                        .padding(end = 18.dp)
                        .size(64.dp),
                )
                Image(
                    painter = painterResource(R.drawable.snake_yellow_blob),
                    contentDescription = null,
                    Modifier.size(64.dp),
                )
            }
            Spacer(Modifier.size(32.dp))
            SnakeGameMenuTextItem(
                Modifier
                    .padding(bottom = 24.dp)
                    .fillMaxColumnWidth(),
                primaryText = stringResource(R.string.vibration_setting),
                secondaryText = enabledText(settings.vibrationEnabled),
                secondaryTextSize = 22.sp,
                onClick = onVibrationSettingClick
            )
            SnakeGameMenuTextItem(
                Modifier
                    .padding(bottom = 24.dp)
                    .fillMaxColumnWidth(),
                primaryText = stringResource(R.string.music_setting),
                secondaryText = enabledText(settings.musicEnabled),
                secondaryTextSize = 22.sp,
                onClick = onMusicSettingClick
            )
            SnakeGameMenuTextItem(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .fillMaxColumnWidth(),
                primaryText = stringResource(R.string.sound_effects_setting),
                secondaryText = enabledText(settings.soundEffectsEnabled),
                secondaryTextSize = 22.sp,
                onClick = onSoundEffectsSettingClick
            )
            SnakeGameMenuTextItem(
                modifier = Modifier.fillMaxColumnWidth(),
                primaryText = stringResource(R.string.language_settings_item),
                secondaryText = when (language) {
                    SnakeGameLanguage.RUSSIAN -> stringResource(R.string.language_russian)
                    SnakeGameLanguage.ENGLISH -> stringResource(R.string.language_english)
                    SnakeGameLanguage.SYSTEM -> stringResource(R.string.language_system)
                },
                secondaryTextSize = 22.sp,
                onClick = onLanguageSettingClick
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

@Composable
private fun enabledText(enabled: Boolean): String {
    val onText = stringResource(R.string.setting_on)
    val offText = stringResource(R.string.setting_off)
    return if (enabled) onText else offText
}

@Preview
@Composable
private fun Preview() {
    SnakeGameSettingsScreen(
        settings = SnakeGameSettings(
            musicEnabled = false
        ),
        language = SnakeGameLanguage.SYSTEM,
        onBackClick = { },
        onVibrationSettingClick = { },
        onMusicSettingClick = { },
        onSoundEffectsSettingClick = { },
        onLanguageSettingClick = { }
    )
}
