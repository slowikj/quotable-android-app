package com.example.quotableapp.common

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <S : Any, D : Any> Flow<PagingData<S>>.mapPagingElements(converter: (S) -> D) =
    this.map { pagingData ->
        pagingData.map { converter(it) }
    }
