package com.example.quotableapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.toModel
import com.example.quotableapp.data.network.model.QuotesResponseDTO

class QuotesPagingSource(private val service: suspend (page: Int, limit: Int) -> QuotesResponseDTO) :
    PagingSource<Int, Quote>() {

    override val keyReuseSupported: Boolean
        get() = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Quote> {
        return try {
            val page = params.key ?: 1
            val response = service(page, params.loadSize)
            LoadResult.Page(
                data = response.results.map { it.toModel() },
                prevKey = null,
                nextKey = if (response.totalPages == page) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Quote>): Int? = null
}