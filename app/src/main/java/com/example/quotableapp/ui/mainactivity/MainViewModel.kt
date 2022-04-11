package com.example.quotableapp.ui.mainactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.common.DispatchersProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    dispatchersProvider: DispatchersProvider
) : ViewModel() {

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch(dispatchersProvider.Default) {
            delay(1000)

            _isLoading.value = false
        }
    }
}