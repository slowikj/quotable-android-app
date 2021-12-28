package com.example.quotableapp.data.repository.quotes.quoteslist.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import retrofit2.Response

class QuotesPagingSource(
    private val service: suspend (page: Int, limit: Int) -> Response<QuotesResponseDTO>
) : PagingSource<Int, QuoteDTO>() {

    override val keyReuseSupported: Boolean
        get() = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, QuoteDTO> {
        return try {
            val page = params.key ?: 1
            val responseBody = service(page, params.loadSize).body()!!
            LoadResult.Page(
                data = responseBody.results,
                prevKey = null,
                nextKey = if (responseBody.totalPages == page) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, QuoteDTO>): Int? = null
}