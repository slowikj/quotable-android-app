package com.example.quotableapp.ui.tagslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.UiStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

typealias TagsListState = UiState<List<Tag>, TagsListViewModel.UiError>

@HiltViewModel
class TagsListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val tagsRepository: TagsRepository
) : ViewModel() {

    sealed class UiError : Throwable() {
        object NetworkError : UiError()
    }

    private val tagsUiStateManager = UiStateManager<List<Tag>, UiError>(
        coroutineScope = viewModelScope,
        sourceDataFlow = tagsRepository.allTagsFlow
    )
    val tagsUiState: StateFlow<TagsListState> = tagsUiStateManager.stateFlow

    init {
        updateTags()
    }

    fun updateTags() {
        tagsUiStateManager.updateData(
            requestFunc = { tagsRepository.updateAllTags() },
            errorTransformer = { UiError.NetworkError }
        )
    }
}
