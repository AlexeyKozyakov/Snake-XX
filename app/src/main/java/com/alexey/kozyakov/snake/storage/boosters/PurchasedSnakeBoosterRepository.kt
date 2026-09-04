package com.alexey.kozyakov.snake.storage.boosters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class PurchasedSnakeBoosterRepository(private val preferencesDataStore: DataStore<Preferences>) {
    fun observe(): Flow<Map<SnakeBooster, Int>> {
        return preferencesDataStore.data.map { preferences ->
            SnakeBooster.entries.associateWith { booster ->
                preferences[booster.countKey] ?: 0
            }
        }
    }

    suspend fun update(calculation: (booster: SnakeBooster, count: Int) -> Int) {
        preferencesDataStore.edit { preferences ->
            for (booster in SnakeBooster.entries) {
                preferences[booster.countKey] =
                    calculation(booster, preferences[booster.countKey] ?: 0)
            }
        }
    }

    private val SnakeBooster.countKey get() = intPreferencesKey("${name}_booster_count")
}
