package com.example.quotableapp.view.tag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.repository.quoteslist.QuotesListRepository
import com.example.quotableapp.di.QuotesType
import com.example.quotableapp.view.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class TagQuotesListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @QuotesType.OfTag tagRepository: QuotesListRepository,
    dispatchers: CoroutineDispatchers
) : QuotesListViewModel(savedStateHandle, tagRepository, dispatchers) {

    companion object {
        const val TAG_ID = "tag"
    }

    override val keyword: String
        get() = savedStateHandle[TAG_ID]!!

    override fun onTagClick(tag: String) {
        if (tag != keyword) {
            viewModelScope.launch {
                _navigationActions.emit(NavigationAction.ToQuotesOfTag(tag))
            }
        }
    }
}