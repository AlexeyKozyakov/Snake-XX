package com.alexey.kozyakov.snake.ui.reference

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuBackButton

private val itemBackgroundColor = Color(0xFF204821)

@Composable
fun SnakeGameReferenceScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        LazyColumn(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(Modifier.statusBarsPadding().padding(top = 18.dp)) }

            headerItem(textResId = R.string.reference_header_how_to_play)

            referenceItem(
                titleResId = R.string.reference_title_grow_snake,
                textResId = R.string.reference_text_grow_snake,
                imageResId = R.drawable.eat_apples
            )
            referenceItem(
                titleResId = R.string.reference_title_pass_levels,
                textResId = R.string.reference_text_pass_levels,
                imageResId = R.drawable.wall_block_0,
                imageSize = 120.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_collect_coins,
                textResId = R.string.reference_text_collect_coins,
                imageResId = R.drawable.coin,
                imageSize = 120.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_avoid_reduction,
                textResId = R.string.reference_text_avoid_reduction,
                imageResId = R.drawable.snake_yellow_xx,
                imageSize = 120.dp
            )

            headerItem(textResId = R.string.reference_header_apple_and_item_types)

            referenceItem(
                titleResId = R.string.reference_title_green_apple,
                textResId = R.string.reference_text_green_apple,
                imageResId = R.drawable.apple_green,
                imageSize = 110.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_red_apple,
                textResId = R.string.reference_text_red_apple,
                imageResId = R.drawable.apple_alt,
                imageSize = 110.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_omnivorousness_apple,
                textResId = R.string.reference_text_omnivorousness_apple,
                imageResId = R.drawable.easter_egg,
                imageSize = 110.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_golden_apple,
                textResId = R.string.reference_text_golden_apple,
                imageResId = R.drawable.apple_gold_64,
                imageSize = 110.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_bad_apple,
                textResId = R.string.reference_text_bad_apple,
                imageResId = R.drawable.oliebol_64,
                imageSize = 110.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_bomb,
                textResId = R.string.reference_text_bomb,
                imageResId = R.drawable.bomb,
                imageSize = 110.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_coin,
                textResId = R.string.reference_text_coin,
                imageResId = R.drawable.coin,
                imageSize = 100.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_diamond,
                textResId = R.string.reference_text_diamond,
                imageResId = R.drawable.diamond,
                imageSize = 100.dp
            )
            item { Spacer(Modifier.navigationBarsPadding()) }
        }

        SnakeGameMenuBackButton(
            onClick = navigateBack,
            Modifier.align(Alignment.TopStart)
        )
    }
}

private fun LazyListScope.headerItem(@StringRes textResId: Int) {
    item {
        Text(
            text = stringResource(textResId),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(16.dp))
    }
}

private fun LazyListScope.referenceItem(
    @StringRes titleResId: Int,
    @StringRes textResId: Int,
    @DrawableRes imageResId: Int,
    imageSize: Dp = 150.dp
) {
    item {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .background(
                    itemBackgroundColor,
                    shape = RoundedCornerShape(36.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(titleResId),
                color = Color.White,
                fontSize = 26.sp,
                fontStyle = FontStyle.Normal,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(imageResId),
                    contentDescription = null,
                    Modifier.size(imageSize).align(Alignment.CenterVertically)
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = stringResource(textResId),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Normal,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SnakeGameReferenceScreen(
        navigateBack = { }
    )
}
