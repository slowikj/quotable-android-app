package com.example.quotableapp.ui.common.uistate

data class UiState<V, E>(
    val isLoading: Boolean = false,
    val data: V? = null,
    val error: E? = null
) {
}
