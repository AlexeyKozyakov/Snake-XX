package com.alexey.kozyakov.snake.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val BALANCE_KEY = intPreferencesKey("balance")

class SnakeGameBalanceRepository(private val preferencesDataStore: DataStore<Preferences>) {
    suspend fun store(balance: Int) {
        preferencesDataStore.edit { preferences ->
            preferences[BALANCE_KEY] = balance
        }
    }

    fun observe(): Flow<Int> {
        return preferencesDataStore.data.map { preferences ->
            preferences[BALANCE_KEY] ?: 0
        }
    }
}
