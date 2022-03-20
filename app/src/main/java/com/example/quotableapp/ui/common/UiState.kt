package com.example.quotableapp.ui.common

data class UiState<V, E>(
    val data: V? = null,
    val isLoading: Boolean = false,
    val error: E? = null
) {
}
