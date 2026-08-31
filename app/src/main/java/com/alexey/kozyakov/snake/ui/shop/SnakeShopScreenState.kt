package com.alexey.kozyakov.snake.ui.shop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import com.alexey.kozyakov.snake.di.balanceRepository
import com.alexey.kozyakov.snake.di.purchaseRepository
import com.alexey.kozyakov.snake.di.snakeSkinRepository
import com.alexey.kozyakov.snake.storage.balance.SnakeGameBalanceRepository
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
        Offer.entries.groupBy { offer ->
            offer.type
        }.map { (type, offers) ->
            SnakeShopCategory(
                nameResId = type.groupNameResId,
                items = offers.map { offer ->
                    SnakeShopItem(
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
                }
            )
        }
    }.asComposeState(initialValue = emptyList())

    val balance by balanceRepository
        .observe()
        .asComposeState(initialValue = 0)

    fun buy(offerId: Int) {
        stateHolderScope.launch {
            val offer = Offer.entries[offerId]
            val purchases = purchaseRepository.observe().first()
            if (offer in purchases) {
                return@launch
            }
            val balance = balanceRepository.observe().first()
            if (balance < offer.price) {
                return@launch
            }
            balanceRepository.update { balance -> balance - offer.price }
            purchaseRepository.add(offer)
            when (offer.type) {
                OfferType.SKIN -> selectSkin(offer)
                OfferType.UPGRADE -> Unit
            }
        }
    }

    fun select(offerId: Int) {
        stateHolderScope.launch {
            val offer = Offer.entries[offerId]
            val purchases = purchaseRepository.observe().first()
            if (offer !in purchases) {
                return@launch
            }
            when (offer.type) {
                OfferType.SKIN -> selectSkin(offer)
                OfferType.UPGRADE -> Unit
            }
        }
    }

    private suspend fun selectSkin(offer: Offer) {
        val skin = SnakeSkin.entries[offer.productId]
        snakeSkinRepository.save(skin)
    }
}

@Composable
fun retainSnakeShopScreenState(): SnakeShopScreenState {
    return retain {
        SnakeShopScreenState(
            snakeSkinRepository,
            purchaseRepository,
            balanceRepository
        )
    }
}
