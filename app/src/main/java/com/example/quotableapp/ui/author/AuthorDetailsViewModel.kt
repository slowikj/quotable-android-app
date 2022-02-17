package com.example.quotableapp.ui.author

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.extensions.handleRequestWithResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    sealed class UiError : Throwable() {
        object IOError : UiError()
    }

    private val _state = MutableStateFlow(AuthorDetailsUiState())
    val state: StateFlow<AuthorDetailsUiState> = _state.asStateFlow()

    init {
        fetchAuthor()
    }

    fun onRefresh() {
        fetchAuthor()
    }

    private fun fetchAuthor() {
        _state.handleRequestWithResult(
            coroutineScope = viewModelScope,
            requestFunc = { authorsRepository.fetchAuthor(authorSlug) },
            errorConverter = { UiError.IOError }
        )
    }

}