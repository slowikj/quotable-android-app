package com.example.quotableapp.ui.common.quoteslist

import androidx.paging.PagingData
import com.example.quotableapp.data.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuotesProvider {

    val quotes: Flow<PagingData<Quote>?>
}