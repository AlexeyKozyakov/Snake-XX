package com.alexey.kozyakov.snake.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexey.kozyakov.R

private val itemBackgroundColor = Color(0xFF204821)
private val selectedItemBorderColor = Color(0xFFFFF216)
private val separatorColor = Color(0xFF547C54)
private val buyButtonColor = Color(0xFF3661FE)

@Composable
fun SnakeShopScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit
) {
    val state = retainSnakeShopScreenState()
    SnakeShopScreen(
        modifier,
        categories = state.categories,
        balance = state.balance,
        onBackClick = navigateBack,
        onBuyClick = state::buy,
        onSelectClick = state::select
    )
}

@Composable
private fun SnakeShopScreen(
    modifier: Modifier = Modifier,
    categories: List<SnakeShopCategory>,
    balance: Int,
    onBackClick: () -> Unit,
    onBuyClick: (offerId: Int) -> Unit,
    onSelectClick: (offerId: Int) -> Unit
) {
    Box(
        modifier
            .background(Color.Black)
            .fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                categories.forEachIndexed { index, category ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            modifier = if (index == 0) {
                                Modifier
                                    .padding(top = 12.dp)
                                    .statusBarsPadding()
                            } else {
                                Modifier
                            },
                            text = stringResource(category.nameResId),
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                    items(
                        category.items,
                        span = { item ->
                            if (item.descriptionResId != null) {
                                GridItemSpan(2)
                            } else {
                                GridItemSpan(1)
                            }
                        },
                        key = { item -> item.offerId }
                    ) { item ->
                        ShopItem(
                            item = item,
                            onSelectClick = { onSelectClick(item.offerId) },
                            onBuyClick = { onBuyClick(item.offerId) }
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.padding(48.dp).navigationBarsPadding())
                }
            }
        }
        Image(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier
                .clickable(enabled = true, onClick = onBackClick)
                .padding(10.dp)
                .statusBarsPadding()
                .size(56.dp)
                .align(Alignment.TopStart)
        )
        Row(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(36.dp)
                .navigationBarsPadding()
                .background(color = buyButtonColor, shape = CircleShape)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = balance.toString(),
                color = Color.White,
                fontSize = 36.sp,
                fontStyle = FontStyle.Normal,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.size(6.dp))
            Image(
                painter = painterResource(R.drawable.coin),
                contentDescription = null,
                Modifier.size(38.dp)
            )
        }
    }
}

@Composable
private fun ShopItem(
    item: SnakeShopItem,
    onSelectClick: () -> Unit,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clickable(
                enabled = item.selectionState == SelectionState.CAN_SELECT,
                onClick = onSelectClick
            )
            .background(
                itemBackgroundColor,
                shape = RoundedCornerShape(36.dp)
            )
            .alpha(if (item.purchaseState == PurchaseState.CANNOT_BUY) 0.4f else 1f)
            .border(
                width = 6.dp,
                color = if (item.selectionState == SelectionState.SELECTED) {
                    selectedItemBorderColor
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(36.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(item.nameResId),
            color = Color.White,
            fontSize = 28.sp,
            fontStyle = FontStyle.Normal,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.size(16.dp))
        Row {
            Image(
                painter = painterResource(item.iconResId),
                contentDescription = null,
                Modifier.size(150.dp)
            )
            if (item.descriptionResId != null) {
                Spacer(Modifier.width(6.dp))
                Spacer(
                    Modifier
                        .background(color = separatorColor)
                        .width(2.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column(Modifier.align(Alignment.CenterVertically)) {
                    Text(
                        text = stringResource(item.descriptionResId),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontStyle = FontStyle.Normal,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.size(12.dp))
                    BuyButton(
                        price = item.price,
                        purchaseState = item.purchaseState,
                        onClick = onBuyClick
                    )
                }
            }
        }
        Spacer(Modifier.size(16.dp))
        if (item.descriptionResId == null) {
            BuyButton(
                Modifier.align(Alignment.CenterHorizontally),
                price = item.price,
                purchaseState = item.purchaseState,
                onClick = onBuyClick
            )
        }
    }
}

@Composable
private fun BuyButton(
    modifier: Modifier = Modifier,
    price: Int,
    purchaseState: PurchaseState,
    onClick: () -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(
                color = buyButtonColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                enabled = purchaseState == PurchaseState.CAN_BUY,
                onClick = onClick
            )
            .alpha(
                if (purchaseState == PurchaseState.BOUGHT) 0.35f else 1.0f
            )
            .padding(12.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = if (purchaseState == PurchaseState.BOUGHT) {
                stringResource(R.string.purchased)
            } else {
                price.toString()
            },
            color = Color.White,
            fontSize = 18.sp,
            fontStyle = FontStyle.Normal,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal
        )
        if (purchaseState != PurchaseState.BOUGHT) {
            Spacer(Modifier.size(8.dp))
            Image(
                painter = painterResource(R.drawable.coin),
                contentDescription = null,
                Modifier.size(20.dp).align(Alignment.CenterVertically)
            )
        }
    }
}
