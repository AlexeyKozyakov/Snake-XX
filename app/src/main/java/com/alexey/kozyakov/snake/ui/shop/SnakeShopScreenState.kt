package com.alexey.kozyakov.snake.ui.shop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import com.alexey.kozyakov.snake.di.balanceRepository
import com.alexey.kozyakov.snake.di.purchaseRepository
import com.alexey.kozyakov.snake.di.snakeSkinRepository
import com.alexey.kozyakov.snake.storage.SnakeGameBalanceRepository
import com.alexey.kozyakov.snake.storage.shop.Offer
import com.alexey.kozyakov.snake.storage.shop.OfferType
import com.alexey.kozyakov.snake.storage.shop.PurchaseRepository
import com.alexey.kozyakov.snake.storage.skins.SnakeSkin
import com.alexey.kozyakov.snake.storage.skins.SnakeSkinRepository
import com.alexey.kozyakov.snake.ui.base.RetainedStateHolder
import com.alexey.kozyakov.snake.ui.base.asComposeState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SnakeShopScreenState(
    private val snakeSkinRepository: SnakeSkinRepository,
    private val purchaseRepository: PurchaseRepository,
    private val balanceRepository: SnakeGameBalanceRepository
) : RetainedStateHolder() {
    val categories by combine(
        snakeSkinRepository.observe(),
        purchaseRepository.observe(),
        balanceRepository.observe()
    ) { selectedSkin, purchases, balance ->
        Offer.entries.map { offer ->
            SnakeShopItem(
                type = offer.type,
                offerId = offer.ordinal,
                price = offer.price,
                iconResId = offer.iconResId,
                nameResId = offer.nameResId,
                descriptionResId = offer.descriptionResId,
                purchaseState = when {
                    offer in purchases -> PurchaseState.BOUGHT
                    balance >= offer.price -> PurchaseState.CAN_BUY
                    else -> PurchaseState.CANNOT_BUY
                },
                selectionState = when (offer.type) {
                    OfferType.UPGRADE -> SelectionState.CANNOT_SELECT
                    OfferType.SKIN -> when {
                        selectedSkin.ordinal == offer.productId -> SelectionState.SELECTED
                        offer in purchases -> SelectionState.CAN_SELECT
                        else -> SelectionState.CANNOT_SELECT
                    }
                }
            )
        }.groupBy { item ->
            item.type
        }.map { (type, items) ->
            SnakeShopCategory(
                nameResId = type.groupNameResId,
                items = items
            )
        }
    }.asComposeState(initialValue = emptyList())

    val balance by balanceRepository
        .observe()
        .asComposeState(initialValue = 0)

    fun buy(offerId: Int) {
        val offer = Offer.entries[offerId]
        stateHolderScope.launch {
            val balance = balanceRepository.observe().first()
            require(balance >= offer.price) { "Not enough money to buy ${offer.name}" }
            val purchases = purchaseRepository.observe().first()
            require(offer !in purchases) { "Cannot buy same offer twice" }
            balanceRepository.store(balance - offer.price)
            purchaseRepository.add(offer)
            if (offer.type == OfferType.SKIN) {
                val skin = SnakeSkin.entries[offer.productId]
                snakeSkinRepository.save(skin)
            }
        }
    }

    fun select(offerId: Int) {
        val offer = Offer.entries[offerId]
        when (offer.type) {
            OfferType.SKIN -> {
                stateHolderScope.launch {
                    val purchases = purchaseRepository.observe().first()
                    require(offer in purchases) { "Cannot select unpurchased offer" }
                    val skin = SnakeSkin.entries[offer.productId]
                    val selectedSkin = snakeSkinRepository.observe().first()
                    require(skin != selectedSkin) { "Skin is already selected" }
                    snakeSkinRepository.save(skin)
                }
            }

            OfferType.UPGRADE -> {
                throw IllegalStateException("Cannot select offer with type: ${OfferType.UPGRADE.name}")
            }
        }
    }
}

@Composable
fun retainSnakeSkinsScreenState(): SnakeShopScreenState {
    return retain {
        SnakeShopScreenState(
            snakeSkinRepository,
            purchaseRepository,
            balanceRepository
        )
    }
}
