package com.example.quotableapp.ui.common.quoteslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.ui.common.OnQuoteClickListener
import com.example.quotableapp.ui.common.formatters.formatToClipboard
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@ExperimentalPagingApi
abstract class QuotesListViewModel constructor(
    protected val savedStateHandle: SavedStateHandle,
    protected val dispatchers: CoroutineDispatchers
) : ViewModel() {

    sealed class Action {

        object Error : Action()
    }

    protected val _actions = MutableSharedFlow<Action>()
    val actions = _actions.asSharedFlow()

    protected val _quotes = MutableStateFlow<PagingData<Quote>?>(null)
    val quotes: StateFlow<PagingData<Quote>?> = _quotes

}