package com.example.quotableapp.data.repository.quotes.onequote

import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.common.HttpApiError

interface OneQuoteRepository {
    suspend fun fetchQuote(id: String): Resource<Quote, HttpApiError>
}
