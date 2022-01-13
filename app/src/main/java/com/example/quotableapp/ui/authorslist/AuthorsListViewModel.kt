package com.example.quotableapp.ui.authorslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class AuthorsListViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authorsRepository: AuthorsRepository,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    sealed class NavigationAction {
        data class ToAuthor(val author: Author) : NavigationAction()
    }

    private val _navigationAction = MutableSharedFlow<NavigationAction>()
    val navigationAction = _navigationAction.asSharedFlow()

    sealed class Action {
        object RefreshList : Action()
    }

    private val _action = MutableSharedFlow<Action>()
    val action = _action.asSharedFlow()

    fun fetchAuthors(): Flow<PagingData<Author>> =
        authorsRepository.fetchAllAuthors()
            .flowOn(dispatchers.IO)
            .cachedIn(viewModelScope)

    fun onAuthorClick(author: Author) {
        viewModelScope.launch {
            _navigationAction.emit(NavigationAction.ToAuthor(author))
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _action.emit(Action.RefreshList)
        }
    }
}