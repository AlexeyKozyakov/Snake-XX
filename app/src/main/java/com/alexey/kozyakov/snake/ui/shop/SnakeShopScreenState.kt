package com.alexey.kozyakov.snake.ui.shop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import com.alexey.kozyakov.BuildConfig
import com.alexey.kozyakov.snake.config.BALANCE_ADD_AMOUNT_DEBUG
import com.alexey.kozyakov.snake.di.balanceRepository
import com.alexey.kozyakov.snake.di.purchaseRepository
import com.alexey.kozyakov.snake.di.purchasedBoosterRepository
import com.alexey.kozyakov.snake.di.snakeSkinRepository
import com.alexey.kozyakov.snake.storage.balance.SnakeGameBalanceRepository
import com.alexey.kozyakov.snake.storage.boosters.PurchasedSnakeBoosterRepository
import com.alexey.kozyakov.snake.storage.boosters.SnakeBooster
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
    private val balanceRepository: SnakeGameBalanceRepository,
    private val boosterRepository: PurchasedSnakeBoosterRepository
) : RetainedStateHolder() {
    val categories by combine(
        snakeSkinRepository.observe(),
        purchaseRepository.observe(),
        balanceRepository.observe(),
        boosterRepository.observe()
    ) { selectedSkin, purchases, balance, boosterCount ->
        Offer.entries.groupBy { offer ->
            offer.type
        }.map { (type, offers) ->
            SnakeShopCategory(
                nameResId = type.groupNameResId,
                items = offers.map { offer ->
                    SnakeShopItem(
                        offerId = offer.ordinal,
                        price = offer.price,
                        count = when (offer.type) {
                            OfferType.SKIN, OfferType.UPGRADE -> null
                            OfferType.BOOSTER -> {
                                val booster = SnakeBooster.entries[offer.productId]
                                boosterCount[booster]
                            }
                        },
                        iconResId = offer.iconResId,
                        nameResId = offer.nameResId,
                        descriptionResId = offer.descriptionResId,
                        purchaseState = when {
                            offer in purchases -> PurchaseState.BOUGHT
                            balance >= offer.price -> PurchaseState.CAN_BUY
                            else -> when (offer.type) {
                                OfferType.SKIN, OfferType.UPGRADE -> PurchaseState.CANNOT_BUY
                                OfferType.BOOSTER -> {
                                    val booster = SnakeBooster.entries[offer.productId]
                                    val count = boosterCount[booster] ?: 0
                                    if (count > 0) {
                                        PurchaseState.CANNOT_BUY_MORE
                                    } else {
                                        PurchaseState.CANNOT_BUY
                                    }
                                }
                            }
                        },
                        selectionState = when (offer.type) {
                            OfferType.UPGRADE, OfferType.BOOSTER -> SelectionState.CANNOT_SELECT
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

    val canAddBalance = BuildConfig.DEBUG

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
            when (offer.type) {
                OfferType.SKIN, OfferType.UPGRADE -> {
                    purchaseRepository.add(offer)
                    when (offer.type) {
                        OfferType.SKIN -> selectSkin(offer)
                        OfferType.UPGRADE -> Unit
                    }
                }

                OfferType.BOOSTER -> {
                    val booster = SnakeBooster.entries[offer.productId]
                    boosterRepository.update(booster) { count -> count + 1 }
                }
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
                OfferType.UPGRADE, OfferType.BOOSTER -> Unit
            }
        }
    }

    fun addBalance() {
        if (!canAddBalance) {
            return
        }
        stateHolderScope.launch {
            balanceRepository.update { balance -> balance + BALANCE_ADD_AMOUNT_DEBUG }
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
            balanceRepository,
            purchasedBoosterRepository
        )
    }
}
