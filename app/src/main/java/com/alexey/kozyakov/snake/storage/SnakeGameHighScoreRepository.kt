package com.alexey.kozyakov.snake.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val HIGH_SCORE_KEY = intPreferencesKey("high_score")

class SnakeGameHighScoreRepository(
    private val preferencesDataStore: DataStore<Preferences>
) {
    suspend fun save(score: Int) {
        preferencesDataStore.edit { preferences ->
            preferences[HIGH_SCORE_KEY] = score
        }
    }

    fun observe(): Flow<Int> {
        return preferencesDataStore.data.map { preferences ->
            preferences[HIGH_SCORE_KEY] ?: 0
        }
    }
}
