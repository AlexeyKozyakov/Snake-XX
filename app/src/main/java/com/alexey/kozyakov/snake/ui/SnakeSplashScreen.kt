package com.alexey.kozyakov.snake.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.ui.components.BlackBox

@Composable
fun SnakeSplashScreen(modifier: Modifier = Modifier) {
    BlackBox(modifier) {
        Image(
            painter = painterResource(R.drawable.snake_head_xx_default),
            contentDescription = null,
            Modifier.align(Alignment.Center).size(160.dp)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    SnakeSplashScreen()
}
