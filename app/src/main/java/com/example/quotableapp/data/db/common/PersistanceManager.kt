package com.example.quotableapp.data.db.common

import androidx.paging.PagingSource

interface PersistenceManager<Entity : Any, PageKey : Any> {

    suspend fun deleteAll()

    suspend fun getLastUpdated(): Long?

    suspend fun getLatestPageKey(): PageKey?

    suspend fun append(entries: List<Entity>, pageKey: PageKey)

    suspend fun <R> withTransaction(block: suspend () -> R): R

    fun getPagingSource(): PagingSource<PageKey, Entity>
}