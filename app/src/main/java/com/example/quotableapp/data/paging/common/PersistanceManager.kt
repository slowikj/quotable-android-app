package com.example.quotableapp.data.paging.common

import androidx.paging.PagingSource

interface PersistenceManager<Entity : Any, PageKey : Any> {

    suspend fun getLastUpdated(): Long?

    suspend fun getLatestPageKey(): PageKey?

    suspend fun append(entities: List<Entity>, pageKey: PageKey)

    suspend fun refresh(entities: List<Entity>, pageKey: PageKey)

    fun getPagingSource(): PagingSource<PageKey, Entity>
}