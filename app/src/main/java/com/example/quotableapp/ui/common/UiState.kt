package com.example.quotableapp.ui.common

data class UiState<V, E>(
    val isLoading: Boolean = false,
    val data: V? = null,
    val error: E? = null
) {
}
