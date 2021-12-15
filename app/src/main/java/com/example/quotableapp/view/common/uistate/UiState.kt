package com.example.quotableapp.view.common.uistate

data class UiState<V, E>(
    val isLoading: Boolean = false,
    val data: V? = null,
    val error: E? = null
) {
}
