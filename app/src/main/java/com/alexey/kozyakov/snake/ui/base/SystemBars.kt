package com.alexey.kozyakov.snake.ui.base

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Activity.hideSystemBars() {
    WindowCompat.setDecorFitsSystemWindows(window, false)

    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).run {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
