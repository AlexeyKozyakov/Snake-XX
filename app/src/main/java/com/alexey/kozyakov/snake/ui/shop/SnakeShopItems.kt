package com.alexey.kozyakov.snake.ui.shop

import com.alexey.kozyakov.snake.storage.shop.OfferType

enum class PurchaseState {
    CAN_BUY,
    CANNOT_BUY,
    BOUGHT
}

enum class SelectionState {
    CAN_SELECT,
    CANNOT_SELECT,
    SELECTED
}

data class SnakeShopItem(
    val type: OfferType,
    val offerId: Int,
    val price: Int,
    val iconResId: Int,
    val nameResId: Int,
    val descriptionResId: Int?,
    val purchaseState: PurchaseState,
    val selectionState: SelectionState
)

data class SnakeShopCategory(
    val nameResId: Int,
    val items: List<SnakeShopItem>
)
