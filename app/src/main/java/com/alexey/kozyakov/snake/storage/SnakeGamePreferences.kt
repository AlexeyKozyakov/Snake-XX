package com.alexey.kozyakov.snake.storage

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.snakeGamePreferences by preferencesDataStore(name = "snake_game_preferences")
