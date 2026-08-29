package com.alexey.kozyakov.snake.storage.shop

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val PURCHASES_KEY = stringSetPreferencesKey("purchases")
private val defaultPurchasesValue = setOf(Offer.SKIN_DEFAULT.name)

class PurchaseRepository(private val preferencesDataStore: DataStore<Preferences>) {
    suspend fun add(offer: Offer) {
        preferencesDataStore.edit { preferences ->
            preferences[PURCHASES_KEY] = preferences[PURCHASES_KEY].orDefault() + offer.name
        }
    }

    fun observe(): Flow<Set<Offer>> {
        return preferencesDataStore.data.map { preferences ->
            preferences[PURCHASES_KEY].orDefault().map { offerId ->
                Offer.valueOf(offerId)
            }.toSet()
        }
    }

    private fun Set<String>?.orDefault(): Set<String> {
        return this ?: defaultPurchasesValue
    }
}
