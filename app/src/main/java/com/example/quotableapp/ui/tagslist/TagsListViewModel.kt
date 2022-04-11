package com.example.quotableapp.ui.tagslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.UiStateManager
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
    private val tagsRepository: TagsRepository,
    private val coroutineDispatchers: CoroutineDispatchers
) : ViewModel() {

    sealed class UiError : Throwable() {
        object IOError : UiError()
    }

    private val _tagsUiStateManager = UiStateManager<List<Tag>, UiError>(
        coroutineScope = viewModelScope + coroutineDispatchers.Default,
        sourceDataFlow = tagsRepository
            .allTagsFlow
            .map { tags -> tags.ifEmpty { null } }
    )
    val tagsUiState: StateFlow<TagsListState> = _tagsUiStateManager.stateFlow

    init {
        updateTagsIfNoData()
    }

    fun updateTags() {
        _tagsUiStateManager.updateData(
            requestFunc = { tagsRepository.updateAllTags() },
            errorTransformer = { UiError.IOError }
        )
    }

    fun consumeError(error: UiError) {
        _tagsUiStateManager.errorFlow.value = null
    }

    private fun updateTagsIfNoData() {
        viewModelScope.launch(coroutineDispatchers.Default) {
            if (tagsRepository.allTagsFlow.first().isEmpty()) {
                updateTags()
            }
        }
    }
}
