package com.alexey.kozyakov.snake.storage.skins

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val SELECTED_SKIN_KEY = intPreferencesKey("selected_snake_skin")

class SnakeSkinRepository(private val preferencesDataStore: DataStore<Preferences>) {
    suspend fun save(skin: SnakeSkin) {
        preferencesDataStore.edit { preferences ->
            preferences[SELECTED_SKIN_KEY] = skin.ordinal
        }
    }

    fun observe(): Flow<SnakeSkin> {
        return preferencesDataStore.data.map { preferences ->
            SnakeSkin.entries[preferences[SELECTED_SKIN_KEY] ?: 0]
        }
    }
}
