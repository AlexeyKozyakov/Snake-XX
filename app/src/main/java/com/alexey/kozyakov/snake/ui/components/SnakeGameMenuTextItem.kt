package com.alexey.kozyakov.snake.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val menuItemBackgroundColor = Color(0xFF00C000)
private val menuItemBorderColor = Color(0xFFFFF216)
private val menuItemCornerRadius = 32.dp

@Composable
fun SnakeGameMenuTextItem(
    modifier: Modifier = Modifier,
    primaryText: String,
    secondaryText: String? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    primaryTextSize: TextUnit = 32.sp,
    secondaryTextSize: TextUnit = 18.sp,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clickable(enabled = enabled, onClick = onClick)
            .background(
                color =if (selected) menuItemBorderColor else menuItemBackgroundColor,
                shape = RoundedCornerShape(menuItemCornerRadius)
            )
            .border(
                width = 6.dp,
                color = menuItemBorderColor,
                shape = RoundedCornerShape(menuItemCornerRadius)
            )
            .padding(24.dp)
            .alpha(if (enabled) 0.75f else 0.15f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = primaryText,
            color = Color.Black,
            fontSize = primaryTextSize,
            fontFamily = FontFamily.Monospace,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center
        )
        if (secondaryText != null) {
            Spacer(Modifier.size(4.dp))
            Text(
                text = secondaryText,
                color = Color.Black.copy(alpha = 0.3f),
                fontSize = secondaryTextSize,
                fontFamily = FontFamily.Monospace,
                fontStyle = FontStyle.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}
