package com.example.quotableapp.data.repository.quotes.quoteslist.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import retrofit2.Response

class QuotesPagingSource(
    private val apiResponseInterpreter: QuotableApiResponseInterpreter,
    private val service: suspend (page: Int, limit: Int) -> Response<QuotesResponseDTO>
) : PagingSource<Int, QuoteDTO>() {

    override val keyReuseSupported: Boolean
        get() = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, QuoteDTO> {
        val page = params.key ?: 1
        return when (val response = apiResponseInterpreter { service(page, params.loadSize) }) {
            is Resource.Success -> LoadResult.Page(
                data = response.value.results,
                prevKey = null,
                nextKey = if (response.value.endOfPaginationReached) null else page + 1
            )
            is Resource.Failure -> LoadResult.Error(
                throwable = response.error
            )
        }
    }

    override fun getRefreshKey(state: PagingState<Int, QuoteDTO>): Int? = null
}