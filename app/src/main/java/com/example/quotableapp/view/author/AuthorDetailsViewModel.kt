package com.example.quotableapp.view.author

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.view.common.uistate.UiState
import com.example.quotableapp.view.common.uistate.setData
import com.example.quotableapp.view.common.uistate.setError
import com.example.quotableapp.view.common.uistate.setLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
typealias AuthorDetailsUiState = UiState<Author, AuthorDetailsViewModel.UiError>

@HiltViewModel
@ExperimentalPagingApi
class AuthorDetailsViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authorsRepository: AuthorsRepository
) : ViewModel() {

    companion object {
        private const val AUTHOR_SLUG_TAG = "authorSlug"
    }

    private val authorSlug: String = savedStateHandle[AUTHOR_SLUG_TAG]!!

    sealed class UiError {
        object IOError : UiError()
    }

    private val _state = MutableStateFlow(AuthorDetailsUiState())
    val state: StateFlow<AuthorDetailsUiState> = _state.asStateFlow()

    init {
        fetchAuthor()
    }

    private fun fetchAuthor() {
        _state.setLoading()
        viewModelScope.launch {
            val res = authorsRepository.fetchAuthor(authorSlug)
            res.onSuccess { _state.setData(it) }
                .onFailure { _state.setError(UiError.IOError) }
        }
    }

}