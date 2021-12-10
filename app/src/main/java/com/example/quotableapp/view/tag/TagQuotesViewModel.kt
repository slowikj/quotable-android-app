package com.example.quotableapp.view.tag

import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.repository.quoteslist.QuotesOfTagRepository
import com.example.quotableapp.view.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class TagQuotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tagRepository: QuotesOfTagRepository
) : QuotesListViewModel(savedStateHandle, tagRepository) {

    companion object {
        const val TAG_ID = "tag"
    }

    override val keyword: String
        get() = savedStateHandle[TAG_ID]!!
}