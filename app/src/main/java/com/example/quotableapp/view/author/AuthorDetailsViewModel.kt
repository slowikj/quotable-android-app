package com.example.quotableapp.view.author

import androidx.lifecycle.*
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.repository.AuthorsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthorDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    authorsRepository: AuthorsRepository
) : ViewModel() {

    companion object {
        private const val AUTHOR_SLUG_TAG = "authorSlug"
    }

    private val authorSlug: String = savedStateHandle[AUTHOR_SLUG_TAG]!!

    private val _author: MutableLiveData<Author> = MutableLiveData()
    val author: LiveData<Author> = _author

    init {
        viewModelScope.launch {
            val res = authorsRepository.fetchAuthor(authorSlug)
            res.onSuccess { _author.postValue(it) }
                .onFailure { // TODO
                }
        }
    }
}