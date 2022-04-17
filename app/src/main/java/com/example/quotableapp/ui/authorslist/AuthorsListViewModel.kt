package com.example.quotableapp.ui.authorslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.usecases.authors.GetAllAuthorsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class AuthorsListViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getAllAuthorsUseCase: GetAllAuthorsUseCase,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel() {

    val authors: Flow<PagingData<Author>> =
        getAllAuthorsUseCase
            .getPagingFlow()
            .cachedIn(viewModelScope)

}