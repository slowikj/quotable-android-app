package com.example.quotableapp.view.allquotes

import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.repository.quoteslist.QuotesListRepository
import com.example.quotableapp.di.QuotesType
import com.example.quotableapp.view.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class AllQuotesListViewModel
@Inject constructor(
    @QuotesType.All quotesListRepository: QuotesListRepository,
    savedStateHandle: SavedStateHandle,
    dispatchers: CoroutineDispatchers
) : QuotesListViewModel(savedStateHandle, quotesListRepository, dispatchers) {

    override val keyword: String
        get() = ""
}