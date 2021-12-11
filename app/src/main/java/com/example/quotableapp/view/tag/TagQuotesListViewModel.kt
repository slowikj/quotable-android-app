package com.example.quotableapp.view.tag

import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.repository.quoteslist.QuotesListRepository
import com.example.quotableapp.di.QuotesType
import com.example.quotableapp.view.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class TagQuotesListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @QuotesType.OfTag tagRepository: QuotesListRepository
) : QuotesListViewModel(savedStateHandle, tagRepository) {

    companion object {
        const val TAG_ID = "tag"
    }

    override val keyword: String
        get() = savedStateHandle[TAG_ID]!!
}