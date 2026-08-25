package com.alexey.kozyakov.snake.ui

import android.app.Application

class SnakeGameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: Application
            private set
    }
}
