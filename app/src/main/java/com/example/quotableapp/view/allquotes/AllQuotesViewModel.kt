package com.example.quotableapp.view.allquotes

import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.repository.quoteslist.AllQuotesRepository
import com.example.quotableapp.view.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class AllQuotesViewModel
@Inject constructor(
    quotesListRepository: AllQuotesRepository,
    savedStateHandle: SavedStateHandle
) : QuotesListViewModel(savedStateHandle, quotesListRepository) {

    override val keyword: String
        get() = ""
}