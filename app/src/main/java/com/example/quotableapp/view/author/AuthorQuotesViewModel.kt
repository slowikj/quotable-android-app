package com.example.quotableapp.view.author

import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.repository.quoteslist.QuotesListRepository
import com.example.quotableapp.di.QuotesType
import com.example.quotableapp.view.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class AuthorQuotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @QuotesType.OfAuthor quotesOfAuthorRepository: QuotesListRepository
) : QuotesListViewModel(savedStateHandle, quotesOfAuthorRepository) {

    companion object {
        const val AUTHOR_KEY = "authorSlug"
    }

    override val keyword: String
        get() = savedStateHandle[AUTHOR_KEY]!!
}