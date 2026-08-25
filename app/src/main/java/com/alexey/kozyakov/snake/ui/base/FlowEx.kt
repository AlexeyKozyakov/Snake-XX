package com.alexey.kozyakov.snake.ui.base

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun<T> Flow<T>.asComposeState(
    scope: CoroutineScope,
    initialValue: T
): MutableState<T> {
    val state = mutableStateOf(initialValue)
    scope.launch {
        collect { value ->
            state.value = value
        }
    }
    return state
}
