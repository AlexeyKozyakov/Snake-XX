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
            painter = painterResource(R.drawable.splash_screen_icon),
            contentDescription = null,
            Modifier.align(Alignment.Center).size(288.dp)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    SnakeSplashScreen()
}
