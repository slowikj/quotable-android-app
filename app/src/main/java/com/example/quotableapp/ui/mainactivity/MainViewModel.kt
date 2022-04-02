package com.example.quotableapp.ui.mainactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.data.repository.tags.TagsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val quotesRepository: QuotesRepository,
    private val authorsRepository: AuthorsRepository,
    private val tagsRepository: TagsRepository
) : ViewModel() {

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val deferredList = listOf(
                async { quotesRepository.updateExemplaryQuotes() },
                async { quotesRepository.updateRandomQuote() },
                async { authorsRepository.updateFirstAuthors() },
                async { tagsRepository.updateAllTags() }
            )

            deferredList.forEach {
                it.join()
            }

            _isLoading.value = false
        }
    }
}