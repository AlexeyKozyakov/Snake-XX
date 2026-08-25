package com.alexey.kozyakov.snake.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun SnakeGameMenuContainer(
    modifier: Modifier = Modifier,
    content: @Composable FlowColumnScope.() -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        FlowColumn(
            Modifier.align(Alignment.Center),
            itemHorizontalAlignment = Alignment.CenterHorizontally,
            horizontalArrangement = Arrangement.spacedBy(42.dp),
            verticalArrangement = Arrangement.Center,
            maxItemsInEachColumn = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                3
            } else {
                Int.MAX_VALUE
            }
        ) {
            content()
        }
    }
}
