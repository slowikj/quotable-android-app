package com.example.quotableapp.data.network.datasources

import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.network.services.QuotesRemoteService
import javax.inject.Inject

interface QuotesRemoteDataSource {
    suspend fun fetch(params: FetchQuoteParams): Result<QuoteDTO>

    suspend fun fetch(params: FetchQuotesListParams): Result<QuotesResponseDTO>

    suspend fun fetch(params: FetchQuotesOfAuthorParams): Result<QuotesResponseDTO>

    suspend fun fetch(params: FetchQuotesOfTagParams): Result<QuotesResponseDTO>

    suspend fun fetch(params: FetchQuotesWithSearchPhrase): Result<QuotesResponseDTO>

    suspend fun fetchRandom(): Result<QuoteDTO>
}

class DefaultQuotesRemoteDataSource @Inject constructor(
    private val apiResponseInterpreter: ApiResponseInterpreter,
    private val remoteService: QuotesRemoteService
) : QuotesRemoteDataSource {

    override suspend fun fetch(params: FetchQuoteParams): Result<QuoteDTO> =
        apiResponseInterpreter.invoke { remoteService.fetchQuote(id = params.id) }


    override suspend fun fetch(params: FetchQuotesListParams): Result<QuotesResponseDTO> =
        apiResponseInterpreter.invoke {
            remoteService.fetchQuotes(
                page = params.page,
                limit = params.limit,
                sortBy = params.sortBy,
                order = params.order
            )
        }


    override suspend fun fetch(params: FetchQuotesOfAuthorParams): Result<QuotesResponseDTO> =
        apiResponseInterpreter.invoke {
            remoteService.fetchQuotesOfAuthor(
                author = params.author,
                page = params.page,
                limit = params.limit
            )
        }


    override suspend fun fetch(params: FetchQuotesOfTagParams): Result<QuotesResponseDTO> =
        apiResponseInterpreter.invoke {
            remoteService.fetchQuotesOfTag(
                tag = params.tag,
                page = params.page,
                limit = params.limit
            )
        }


    override suspend fun fetch(params: FetchQuotesWithSearchPhrase): Result<QuotesResponseDTO> =
        apiResponseInterpreter.invoke {
            remoteService.fetchQuotesWithSearchPhrase(
                searchPhrase = params.searchPhrase,
                page = params.page,
                limit = params.limit
            )
        }


    override suspend fun fetchRandom(): Result<QuoteDTO> =
        apiResponseInterpreter.invoke {
            remoteService.fetchRandomQuote()
        }
}

data class FetchQuoteParams(
    val id: String
)

data class FetchQuotesListParams(
    val page: Int,
    val limit: Int,
    val sortBy: QuotesRemoteService.SortByType = QuotesRemoteService.SortByType.Author,
    val order: QuotesRemoteService.OrderType = QuotesRemoteService.OrderType.Asc
)

data class FetchQuotesOfAuthorParams(
    val author: String,
    val page: Int,
    val limit: Int
)

data class FetchQuotesOfTagParams(
    val tag: String,
    val page: Int,
    val limit: Int
)

data class FetchQuotesWithSearchPhrase(
    val searchPhrase: String,
    val page: Int,
    val limit: Int
)