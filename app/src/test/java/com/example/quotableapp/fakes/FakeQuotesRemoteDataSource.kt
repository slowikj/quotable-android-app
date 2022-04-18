package com.example.quotableapp.fakes

import com.example.quotableapp.data.remote.datasources.*
import com.example.quotableapp.data.remote.model.QuoteDTO
import com.example.quotableapp.data.remote.model.QuotesResponseDTO
import kotlinx.coroutines.CompletableDeferred

class FakeQuotesRemoteDataSource(
    val fetchQuoteCompletableDeferred: CompletableDeferred<Result<QuoteDTO>> = CompletableDeferred(),
    val fetchQuotesListCompletableDeferred: CompletableDeferred<Result<QuotesResponseDTO>> = CompletableDeferred(),
    val fetchQuotesOfAuthorCompletableDeferred: CompletableDeferred<Result<QuotesResponseDTO>> = CompletableDeferred(),
    val fetchQuotesOfTagCompletableDeferred: CompletableDeferred<Result<QuotesResponseDTO>> = CompletableDeferred(),
    val fetchQuotesWithSearchPhraseCompletableDeferred: CompletableDeferred<Result<QuotesResponseDTO>> = CompletableDeferred(),
    val fetchRandomQuoteCompletableDeferred: CompletableDeferred<Result<QuoteDTO>> = CompletableDeferred(),
) : QuotesRemoteDataSource {
    override suspend fun fetch(params: FetchQuoteParams): Result<QuoteDTO> {
        return fetchQuoteCompletableDeferred.await()
    }

    override suspend fun fetch(params: FetchQuotesListParams): Result<QuotesResponseDTO> {
        return fetchQuotesListCompletableDeferred.await()
    }

    override suspend fun fetch(params: FetchQuotesOfAuthorParams): Result<QuotesResponseDTO> {
        return fetchQuotesOfAuthorCompletableDeferred.await()
    }

    override suspend fun fetch(params: FetchQuotesOfTagParams): Result<QuotesResponseDTO> {
        return fetchQuotesOfTagCompletableDeferred.await()
    }

    override suspend fun fetch(params: FetchQuotesWithSearchPhrase): Result<QuotesResponseDTO> {
        return fetchQuotesWithSearchPhraseCompletableDeferred.await()
    }

    override suspend fun fetchRandom(): Result<QuoteDTO> {
        return fetchRandomQuoteCompletableDeferred.await()
    }
}