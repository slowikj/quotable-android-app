package com.example.quotableapp.ui.common.uistate

import kotlinx.coroutines.flow.MutableStateFlow

fun <V, E> MutableStateFlow<UiState<V, E>>.setLoading() {
    value = value.copy(isLoading = true)
}

fun <V, E> MutableStateFlow<UiState<V, E>>.setData(data: V) {
    value = value.copy(
        isLoading = false,
        error = null,
        data = data
    )
}

fun <V, E> MutableStateFlow<UiState<V, E>>.setError(error: E) {
    value = value.copy(
        isLoading = false,
        error = error
    )
}