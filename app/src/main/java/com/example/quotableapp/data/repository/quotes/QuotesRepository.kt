package com.example.quotableapp.data.repository.quotes

import com.example.quotableapp.data.repository.quotes.onequote.OneQuoteRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.AllQuotesRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.QuotesOfAuthorRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.QuotesOfTagRepository
import javax.inject.Inject

interface QuotesRepository :
    AllQuotesRepository, QuotesOfAuthorRepository, QuotesOfTagRepository,
    OneQuoteRepository

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