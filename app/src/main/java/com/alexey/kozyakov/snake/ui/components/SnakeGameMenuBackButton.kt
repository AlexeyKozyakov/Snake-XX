package com.alexey.kozyakov.snake.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp

@Composable
fun SnakeGameMenuBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Image(
        imageVector = Icons.AutoMirrored.Default.ArrowBack,
        contentDescription = null,
        colorFilter = ColorFilter.tint(Color.White),
        modifier = modifier
            .clickable(enabled = true, onClick = onClick)
            .padding(10.dp)
            .statusBarsPadding()
            .size(56.dp)
    )
}
