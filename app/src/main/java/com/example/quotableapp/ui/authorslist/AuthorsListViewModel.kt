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
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class AuthorsListViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authorsRepository: AuthorsRepository,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    val authors: Flow<PagingData<Author>> =
        authorsRepository.fetchAllAuthors()
            .flowOn(dispatchers.IO)
            .cachedIn(viewModelScope)

}