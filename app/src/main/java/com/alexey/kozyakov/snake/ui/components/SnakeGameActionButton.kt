package com.alexey.kozyakov.snake.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@Composable
fun SnakeGameActionButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alpha: Float = if (enabled) 1.0f else 0.3f,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color = blueColor)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(alpha),
        content = content
    )
}
