package com.alexey.kozyakov.snake.storage.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
private val SOUND_EFFECTS_ENABLED_KEY = booleanPreferencesKey("sound_effects_enabled")

class SnakeGameSettingsRepository(private val preferencesDataStore: DataStore<Preferences>) {
    suspend fun save(settings: SnakeGameSettings) {
        preferencesDataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED_KEY] = settings.vibrationEnabled
            preferences[MUSIC_ENABLED_KEY] = settings.musicEnabled
            preferences[SOUND_EFFECTS_ENABLED_KEY] = settings.soundEffectsEnabled
        }
    }

    fun observe(): Flow<SnakeGameSettings> {
        return preferencesDataStore.data.map { data ->
            SnakeGameSettings(
                vibrationEnabled = data[VIBRATION_ENABLED_KEY] ?: true,
                musicEnabled = data[MUSIC_ENABLED_KEY] ?: true,
                soundEffectsEnabled = data[SOUND_EFFECTS_ENABLED_KEY] ?: true,
            )
        }
    }
}
