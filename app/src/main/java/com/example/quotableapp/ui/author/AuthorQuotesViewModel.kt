package com.example.quotableapp.ui.author

import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.repository.quotes.di.QuotesType
import com.example.quotableapp.data.repository.quotes.quoteslist.QuotesListRepository
import com.example.quotableapp.ui.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class AuthorQuotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @QuotesType.OfAuthor quotesOfAuthorRepository: QuotesListRepository,
    dispatchers: CoroutineDispatchers
) : QuotesListViewModel(savedStateHandle, quotesOfAuthorRepository, dispatchers) {

    companion object {
        const val AUTHOR_KEY = "authorSlug"
    }

    override val keyword: String
        get() = savedStateHandle[AUTHOR_KEY]!!
}