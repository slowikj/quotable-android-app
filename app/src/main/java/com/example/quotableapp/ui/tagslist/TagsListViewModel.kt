package com.example.quotableapp.ui.tagslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.UiStateManager
import com.example.quotableapp.usecases.tags.GetAllTagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

typealias TagsListState = UiState<List<Tag>, TagsListViewModel.UiError>

@HiltViewModel
class TagsListViewModel @Inject constructor(
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel() {

    sealed class UiError : Throwable() {
        object IOError : UiError()
    }

    private val _tagsUiStateManager = UiStateManager<List<Tag>, UiError>(
        coroutineScope = viewModelScope + dispatchersProvider.Default,
        sourceDataFlow = getAllTagsUseCase
            .flow
            .map { tags -> tags.ifEmpty { null } }
    )
    val tagsUiState: StateFlow<TagsListState> = _tagsUiStateManager.stateFlow

    init {
        updateTagsIfNoData()
    }

    fun updateTags() {
        _tagsUiStateManager.updateData(
            requestFunc = { getAllTagsUseCase.update() },
            errorTransformer = { UiError.IOError }
        )
    }

    fun consumeError(error: UiError) {
        _tagsUiStateManager.errorFlow.value = null
    }

    private fun updateTagsIfNoData() {
        viewModelScope.launch(dispatchersProvider.Default) {
            if (getAllTagsUseCase.flow.first().isEmpty()) {
                updateTags()
            }
        }
    }
}
