package com.alexey.kozyakov.snake.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.ui.components.BlackBox
import com.alexey.kozyakov.snake.ui.components.MonospaceText
import com.alexey.kozyakov.snake.ui.components.SnakeGameActionButton
import com.alexey.kozyakov.snake.ui.components.SnakeGameMenuBackButton

private val itemBackgroundColor = Color(0xFF204821)
private val selectedItemBorderColor = Color(0xFFFFF216)
private val separatorColor = Color(0xFF547C54)
private val blueColor = Color(0xFF3661FE)

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
        balanceLongClickEnabled = state.canAddBalance,
        onBackClick = navigateBack,
        onBuyClick = state::buy,
        onSelectClick = state::select,
        onBalanceLongClick = state::addBalance
    )
}

@Composable
private fun SnakeShopScreen(
    modifier: Modifier = Modifier,
    categories: List<SnakeShopCategory>,
    balance: Int,
    balanceLongClickEnabled: Boolean,
    onBackClick: () -> Unit,
    onBuyClick: (offerId: Int) -> Unit,
    onSelectClick: (offerId: Int) -> Unit,
    onBalanceLongClick: () -> Unit
) {
    BlackBox(modifier) {
        ShopItemsGrid {
            categories.forEachIndexed { index, category ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoryHeader(
                        nameResId = category.nameResId,
                        modifier = if (index == 0) {
                            Modifier
                                .padding(top = 12.dp)
                                .statusBarsPadding()
                        } else {
                            Modifier
                        }
                    )
                }
                items(
                    category.items,
                    span = { item ->
                        GridItemSpan(if (item.descriptionResId != null) 2 else 1)
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
                Spacer(
                    Modifier
                        .padding(48.dp)
                        .navigationBarsPadding()
                )
            }
        }
        CurrentBalance(
            balance = balance,
            longClickEnabled = balanceLongClickEnabled,
            onLongClick = onBalanceLongClick
        )
        SnakeGameMenuBackButton(onClick = onBackClick)
    }
}

@Composable
private fun ShopItemsGrid(
    modifier: Modifier = Modifier,
    content: LazyGridScope.() -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        content = content
    )
}

@Composable
private fun CategoryHeader(
    nameResId: Int,
    modifier: Modifier = Modifier
) {
    MonospaceText(
        modifier = modifier,
        text = stringResource(nameResId),
        fontSize = 38.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
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
        Row(
            Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonospaceText(
                text = stringResource(item.nameResId),
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
            if (item.count != null) {
                Spacer(Modifier.size(8.dp))
                MonospaceText(
                    text = item.count.toString(),
                    modifier = Modifier
                        .background(blueColor, shape = CircleShape)
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                )
            }
        }
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
                    MonospaceText(
                        text = stringResource(item.descriptionResId),
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
    SnakeGameActionButton(
        modifier,
        enabled = purchaseState == PurchaseState.CAN_BUY,
        alpha = when (purchaseState) {
            PurchaseState.BOUGHT, PurchaseState.CANNOT_BUY_MORE -> 0.3f
            else -> 1.0f
        },
        onClick = onClick
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            MonospaceText(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = if (purchaseState == PurchaseState.BOUGHT) {
                    stringResource(R.string.purchased)
                } else {
                    price.toString()
                }
            )
            if (purchaseState != PurchaseState.BOUGHT) {
                Spacer(Modifier.size(8.dp))
                Image(
                    painter = painterResource(R.drawable.coin),
                    contentDescription = null,
                    Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun BoxScope.CurrentBalance(
    balance: Int,
    longClickEnabled: Boolean,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .align(Alignment.BottomEnd)
            .padding(26.dp)
            .navigationBarsPadding()
            .clip(CircleShape)
            .background(color = blueColor)
            .combinedClickable(
                enabled = longClickEnabled,
                onClick = { },
                onLongClick = onLongClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonospaceText(
            text = balance.toString(),
            fontSize = 36.sp
        )
        Spacer(Modifier.size(6.dp))
        Image(
            painter = painterResource(R.drawable.coin),
            contentDescription = null,
            Modifier.size(38.dp)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    SnakeShopScreen(
        categories = listOf(
            SnakeShopCategory(
                nameResId = R.string.offer_group_upgrades,
                items = listOf(
                    SnakeShopItem(
                        offerId = 0,
                        price = 50,
                        count = null,
                        iconResId = R.drawable.upgrade_coins_for_apples,
                        nameResId = R.string.upgrade_name_coins_for_apples,
                        descriptionResId = R.string.upgrade_description_coins_for_apples,
                        purchaseState = PurchaseState.CAN_BUY,
                        selectionState = SelectionState.CANNOT_SELECT
                    ),
                    SnakeShopItem(
                        offerId = 1,
                        price = 250,
                        count = null,
                        iconResId = R.drawable.upgrade_coins_for_levels,
                        nameResId = R.string.upgrade_name_coins_for_levels,
                        descriptionResId = R.string.upgrade_description_coins_for_levels,
                        purchaseState = PurchaseState.BOUGHT,
                        selectionState = SelectionState.CANNOT_SELECT
                    )
                )
            )
        ),
        balance = 128,
        balanceLongClickEnabled = false,
        onBackClick = { },
        onBuyClick = { },
        onSelectClick = { },
        onBalanceLongClick = { }
    )
}