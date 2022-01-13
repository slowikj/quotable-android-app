package com.example.quotableapp.ui.tagslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.example.quotableapp.ui.common.uistate.UiState
import com.example.quotableapp.ui.common.uistate.setData
import com.example.quotableapp.ui.common.uistate.setError
import com.example.quotableapp.ui.common.uistate.setLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias TagsListState = UiState<List<Tag>, TagsListViewModel.UiError>

@HiltViewModel
class TagsListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val tagsRepository: TagsRepository
) : ViewModel() {

    sealed class UiError {
        object NetworkError : UiError()
    }

    sealed class NavigationAction {
        data class ToTagQuotes(val tag: Tag) : NavigationAction()
    }

    private val _navigationActions = MutableSharedFlow<NavigationAction>()
    val navigationActions: SharedFlow<NavigationAction> = _navigationActions.asSharedFlow()

    private val _tags = MutableStateFlow(TagsListState())
    val tags: StateFlow<TagsListState> = _tags.asStateFlow()

    init {
        fetchTags()
    }

    fun fetchTags() {
        viewModelScope.launch {
            _tags.setLoading()
            val response = tagsRepository.fetchAllTags()
            response.onSuccess {
                _tags.setData(it)
            }.onFailure {
                _tags.setError(UiError.NetworkError)
            }
        }
    }

    fun onTagClick(tag: Tag) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToTagQuotes(tag))
        }
    }
}