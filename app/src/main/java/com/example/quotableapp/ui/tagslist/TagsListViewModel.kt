package com.example.quotableapp.ui.tagslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.example.quotableapp.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private val _tagsErrorFlow = MutableStateFlow<UiError?>(null)
    private val _tagsIsLoadingFlow = MutableStateFlow<Boolean>(false)
    private val _tagsListFlow = tagsRepository
        .allTagsFlow
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    val tags = combine(
        _tagsListFlow, _tagsIsLoadingFlow, _tagsErrorFlow
    ) { list, isLoading, error ->
        TagsListState(data = list, isLoading = isLoading, error = error)
    }

    init {
        updateTags()
    }

    fun updateTags() {
        viewModelScope.launch {
            _tagsIsLoadingFlow.value = true
            val response = tagsRepository.updateAllTags()
            response.onFailure {
                _tagsErrorFlow.value = UiError.NetworkError
            }
            _tagsIsLoadingFlow.value = false
        }
    }
}
