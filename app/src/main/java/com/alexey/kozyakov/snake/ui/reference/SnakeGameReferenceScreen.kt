package com.alexey.kozyakov.snake.ui.reference

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.ui.components.BlackBox
import com.alexey.kozyakov.snake.ui.components.MonospaceText
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuBackButton

private val itemBackgroundColor = Color(0xFF204821)

@Composable
fun SnakeGameReferenceScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit
) {
    BlackBox(modifier) {
        ReferenceGrid {
            headerItem(textResId = R.string.reference_header_how_to_play)

            referenceItem(
                titleResId = R.string.reference_title_grow_snake,
                textResId = R.string.reference_text_grow_snake,
                imageResId = R.drawable.eat_apples,
                imageSize = 120.dp,
            )
            referenceItem(
                titleResId = R.string.reference_title_pass_levels,
                textResId = R.string.reference_text_pass_levels,
                imageResId = R.drawable.wall_block_0,
                imageSize = 100.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_collect_coins,
                textResId = R.string.reference_text_collect_coins,
                imageResId = R.drawable.coin,
                imageSize = 100.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_avoid_reduction,
                textResId = R.string.reference_text_avoid_reduction,
                imageResId = R.drawable.snake_yellow_xx,
                imageSize = 100.dp
            )

            headerItem(textResId = R.string.reference_header_apple_and_item_types)

            referenceItem(
                titleResId = R.string.reference_title_green_apple,
                textResId = R.string.reference_text_green_apple,
                imageResId = R.drawable.apple_green,
                imageSize = 70.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_red_apple,
                textResId = R.string.reference_text_red_apple,
                imageResId = R.drawable.apple_alt,
                imageSize = 70.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_omnivorousness_apple,
                textResId = R.string.reference_text_omnivorousness_apple,
                imageResId = R.drawable.easter_egg,
                imageSize = 70.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_golden_apple,
                textResId = R.string.reference_text_golden_apple,
                imageResId = R.drawable.apple_gold_64,
                imageSize = 70.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_bad_apple,
                textResId = R.string.reference_text_bad_apple,
                imageResId = R.drawable.oliebol_64,
                imageSize = 70.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_bomb,
                textResId = R.string.reference_text_bomb,
                imageResId = R.drawable.bomb,
                imageSize = 70.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_coin,
                textResId = R.string.reference_text_coin,
                imageResId = R.drawable.coin,
                imageSize = 70.dp
            )
            referenceItem(
                titleResId = R.string.reference_title_diamond,
                textResId = R.string.reference_text_diamond,
                imageResId = R.drawable.diamond,
                imageSize = 70.dp
            )
        }

        SnakeGameMenuBackButton(onClick = navigateBack)
    }
}

@Composable
private fun ReferenceGrid(
    modifier: Modifier = Modifier,
    content: LazyGridScope.() -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(
                Modifier
                    .statusBarsPadding()
                    .padding(top = 18.dp)
            )
        }
        content()
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

private fun LazyGridScope.headerItem(@StringRes textResId: Int) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MonospaceText(
                text = stringResource(textResId),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(16.dp))
        }
    }
}

private fun LazyGridScope.referenceItem(
    @StringRes titleResId: Int,
    @StringRes textResId: Int,
    @DrawableRes imageResId: Int,
    imageSize: Dp
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
            MonospaceText(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(titleResId),
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(imageResId),
                    contentDescription = null,
                    Modifier
                        .size(imageSize)
                        .align(Alignment.CenterVertically)
                )
                Spacer(Modifier.width(14.dp))
                MonospaceText(
                    text = stringResource(textResId),
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
