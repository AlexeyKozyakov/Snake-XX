package com.alexey.kozyakov.snake.storage.upgrade

import com.alexey.kozyakov.snake.storage.shop.OfferType
import com.alexey.kozyakov.snake.storage.shop.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SnakeUpgradeRepository(private val purchaseRepository: PurchaseRepository) {
    fun observe(): Flow<Set<SnakeUpgrade>> {
        return purchaseRepository.observe().map { offers ->
            offers
                .filter { offer -> offer.type == OfferType.UPGRADE }
                .map { offer -> SnakeUpgrade.entries[offer.productId] }
                .toSet()
        }
    }
}
