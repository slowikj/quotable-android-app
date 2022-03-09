package com.example.quotableapp.ui.tagslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.extensions.handleOneShotRequest
import com.example.quotableapp.ui.common.extensions.set
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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

    private val _tags = MutableStateFlow(TagsListState())
    val tags: StateFlow<TagsListState> = _tags.asStateFlow()

    init {
        fetchTags(forceRefresh = false)
        startObservingTagsFlow()
    }

    fun fetchTags(forceRefresh: Boolean = true) {
        _tags.handleOneShotRequest(
            coroutineScope = viewModelScope,
            requestFunc = { tagsRepository.fetchAllTags(forceRefresh) },
            errorConverter = { UiError.NetworkError }
        )
    }

    private fun startObservingTagsFlow() {
        tagsRepository.allTagsFlow
            .onEach { _tags.set(data = it) }
            .launchIn(viewModelScope)
    }
}