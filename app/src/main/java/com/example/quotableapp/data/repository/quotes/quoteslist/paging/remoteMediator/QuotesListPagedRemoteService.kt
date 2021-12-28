package com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator

import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import retrofit2.Response
import javax.inject.Inject

class DefaultQuotesListRemoteService @Inject constructor(private val quotesService: QuotesService) :
    IntPagedRemoteService<QuotesResponseDTO> {
    override suspend fun fetch(page: Int, limit: Int): Response<QuotesResponseDTO> {
        return quotesService.fetchQuotes(page = page, limit = limit)
    }
}