package com.example.quotableapp.view.onequote

import androidx.lifecycle.*
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.OneQuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OneQuoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val oneQuoteRepository: OneQuoteRepository
) : ViewModel() {

    companion object {
        const val QUOTE_ID = "quoteId"
    }

    private val quoteId: String = savedStateHandle[QUOTE_ID]!!

    private val _quote: MutableLiveData<Quote> = MutableLiveData()
    val quote: LiveData<Quote> = _quote

    init {
        viewModelScope.launch {
            _quote.postValue(oneQuoteRepository.fetchQuote(quoteId))
        }
    }
}