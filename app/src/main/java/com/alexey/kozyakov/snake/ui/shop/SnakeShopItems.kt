package com.alexey.kozyakov.snake.ui.shop

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class PurchaseState {
    CAN_BUY,
    CANNOT_BUY,
    CANNOT_BUY_MORE,
    BOUGHT
}

enum class SelectionState {
    CAN_SELECT,
    CANNOT_SELECT,
    SELECTED
}

data class SnakeShopItem(
    val offerId: Int,
    val price: Int,
    val count: Int?,
    @DrawableRes val iconResId: Int,
    @StringRes val nameResId: Int,
    @StringRes val descriptionResId: Int?,
    val purchaseState: PurchaseState,
    val selectionState: SelectionState
)

data class SnakeShopCategory(
    @StringRes val nameResId: Int,
    val items: List<SnakeShopItem>
)
