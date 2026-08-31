package com.alexey.kozyakov.snake.storage.base

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class CachedDataStore<T: Any>(private val source: DataStore<T>): DataStore<T> {
    private val cache = MutableStateFlow<T?>(null)

    override val data: Flow<T> = cache.filterNotNull()

    suspend fun init() {
        cache.value = source.data.first()
    }

    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        val updatedValue = source.updateData(transform)
        cache.value = updatedValue
        return updatedValue
    }
}
