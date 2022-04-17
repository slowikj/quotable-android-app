package com.example.quotableapp.ui.tagquotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.ui.common.quoteslist.QuotesProvider
import com.example.quotableapp.usecases.quotes.GetQuotesOfTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class TagQuotesListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getQuotesOfTagUseCase: GetQuotesOfTagUseCase,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel(), QuotesProvider {

    companion object {
        const val TAG_ID = "tag"
    }

    val tagName: String
        get() = savedStateHandle[TAG_ID]!!

    override val quotes: Flow<PagingData<Quote>> =
        getQuotesOfTagUseCase
            .getPagingFlow(tagName)
            .cachedIn(viewModelScope)
}