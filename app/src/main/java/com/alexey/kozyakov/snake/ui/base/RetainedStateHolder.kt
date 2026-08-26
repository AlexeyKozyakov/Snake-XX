package com.alexey.kozyakov.snake.ui.base

import androidx.compose.runtime.State
import androidx.compose.runtime.retain.RetainObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow

open class RetainedStateHolder : RetainObserver {
    val stateHolderScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onRetained() = Unit

    override fun onEnteredComposition() = Unit

    override fun onExitedComposition() = Unit

    override fun onRetired() {
        dispose()
    }

    override fun onUnused() {
        dispose()
    }

    open fun dispose() {
        stateHolderScope.cancel()
    }
}

context(stateHolder: RetainedStateHolder)
fun<T> Flow<T>.asComposeState(initialValue: T): State<T> {
    return asComposeState(stateHolder.stateHolderScope, initialValue)
}
