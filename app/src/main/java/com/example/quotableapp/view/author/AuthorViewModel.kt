package com.example.quotableapp.view.author

import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.repository.quoteslist.QuotesOfAuthorRepository
import com.example.quotableapp.view.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class AuthorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    quotesOfAuthorRepository: QuotesOfAuthorRepository
) : QuotesListViewModel(savedStateHandle, quotesOfAuthorRepository) {

    companion object {
        const val AUTHOR_KEY = "authorSlug"
    }

    override val keyword: String
        get() = savedStateHandle[AUTHOR_KEY]!!
}