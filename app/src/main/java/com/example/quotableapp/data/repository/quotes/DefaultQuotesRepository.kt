package com.example.quotableapp.data.repository.quotes

import com.example.quotableapp.data.repository.quotes.onequote.OneQuoteRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.all.AllQuotesRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.ofauthor.QuotesOfAuthorRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.oftag.QuotesOfTagRepository
import javax.inject.Inject

class DefaultQuotesRepository @Inject constructor(
    private val allQuotesRepository: AllQuotesRepository,
    private val quotesOfAuthorRepository: QuotesOfAuthorRepository,
    private val quotesOfTagRepository: QuotesOfTagRepository,
    private val oneQuoteRepository: OneQuoteRepository
) : AllQuotesRepository by allQuotesRepository,
    QuotesOfAuthorRepository by quotesOfAuthorRepository,
    QuotesOfTagRepository by quotesOfTagRepository,
    OneQuoteRepository by oneQuoteRepository,
    QuotesRepository {
}