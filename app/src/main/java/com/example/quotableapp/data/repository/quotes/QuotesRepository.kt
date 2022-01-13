package com.example.quotableapp.data.repository.quotes

import com.example.quotableapp.data.repository.quotes.onequote.OneQuoteRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.all.AllQuotesRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.ofauthor.QuotesOfAuthorRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.oftag.QuotesOfTagRepository

interface QuotesRepository :
    AllQuotesRepository, QuotesOfAuthorRepository, QuotesOfTagRepository,
    OneQuoteRepository