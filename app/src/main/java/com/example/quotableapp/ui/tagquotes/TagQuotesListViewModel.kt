package com.example.quotableapp.ui.tagquotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.ui.common.quoteslist.QuotesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class TagQuotesListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quotesRepository: QuotesRepository,
    private val dispatchers: CoroutineDispatchers
) : ViewModel(), QuotesProvider {

    companion object {
        const val TAG_ID = "tag"
    }

    val tagName: String
        get() = savedStateHandle[TAG_ID]!!

    override val quotes: Flow<PagingData<Quote>?>
        get() = quotesRepository.fetchQuotesOfTag(tagName)
            .stateIn(
                initialValue = null,
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000)
            )
}