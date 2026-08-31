package com.alexey.kozyakov.snake.ui.shop

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
