package com.alexey.kozyakov.snake.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.storage.language.SnakeGameLanguage
import com.alexey.kozyakov.snake.storage.settings.SnakeGameSettings
import com.alexey.kozyakov.snake.storage.skins.SnakeSkin
import com.alexey.kozyakov.snake.ui.components.MonospaceText
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuBackButton
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
        skin = state.skin,
        onBackClick = navigateBack,
        onVibrationSettingClick = state::toggleVibration,
        onMusicSettingClick = state::toggleMusic,
        onSoundEffectsSettingClick = state::toggleSoundEffects,
        onLanguageSettingClick = navigateToLanguageSettings
    )
}

@Composable
fun SnakeGameSettingsScreen(
    modifier: Modifier = Modifier,
    settings: SnakeGameSettings,
    language: SnakeGameLanguage,
    skin: SnakeSkin,
    onBackClick: () -> Unit,
    onVibrationSettingClick: () -> Unit,
    onMusicSettingClick: () -> Unit,
    onSoundEffectsSettingClick: () -> Unit,
    onLanguageSettingClick: () -> Unit
) {
    Box(modifier.fillMaxSize()) {
        SnakeGameMenuContainer(Modifier.align(Alignment.Center)) {
            SettingsHeader(skin)
            Spacer(Modifier.size(32.dp))
            SettingsItems(
                settings = settings,
                language = language,
                onVibrationSettingClick = onVibrationSettingClick,
                onMusicSettingClick = onMusicSettingClick,
                onSoundEffectsSettingClick = onSoundEffectsSettingClick,
                onLanguageSettingClick = onLanguageSettingClick
            )
        }
        SnakeGameMenuBackButton(onClick = onBackClick)
    }
}

@Composable
private fun FlowColumnScope.SettingsHeader(skin: SnakeSkin) {
    MonospaceText(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(bottom = 38.dp),
        text = stringResource(R.string.settings_screen),
        fontSize = 42.sp,
        fontWeight = FontWeight.Bold
    )
    Row {
        Image(
            painter = painterResource(skin.headXXResId),
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowColumnScope.SettingsItems(
    settings: SnakeGameSettings,
    language: SnakeGameLanguage,
    onVibrationSettingClick: () -> Unit,
    onMusicSettingClick: () -> Unit,
    onSoundEffectsSettingClick: () -> Unit,
    onLanguageSettingClick: () -> Unit
) {
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
            .fillMaxColumnWidth(),
        primaryText = stringResource(R.string.music_setting),
        secondaryText = enabledText(settings.musicEnabled),
        secondaryTextSize = 22.sp,
        onClick = onMusicSettingClick
    )
    Spacer(Modifier.size(24.dp))
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
        secondaryText = stringResource(language.nameResId),
        secondaryTextSize = 22.sp,
        onClick = onLanguageSettingClick
    )
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
        skin = SnakeSkin.SLIME,
        onBackClick = { },
        onVibrationSettingClick = { },
        onMusicSettingClick = { },
        onSoundEffectsSettingClick = { },
        onLanguageSettingClick = { }
    )
}
