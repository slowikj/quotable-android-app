package com.example.quotableapp.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.toModel
import com.example.quotableapp.data.networking.QuotesService
import java.lang.Exception

class QuotesPagingSource(private val quotesService: QuotesService) : PagingSource<Int, Quote>() {

    override val keyReuseSupported: Boolean
        get() = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Quote> {
        return try {
            val key = params.key ?: 1
            val response = quotesService.fetchQuotes(page = key, limit = params.loadSize)
            Log.d(
                this::class.java.name,
                "key: $key, itemsCnt: ${response.results.size}, page: ${response.page}"
            )
            LoadResult.Page(
                data = response.results.map { it.toModel() },
                prevKey = null,
                nextKey = if (response.totalPages == response.page) null else response.page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Quote>): Int? {
        return null
    }
}