package com.example.quotableapp.ui.common.uistate

import com.example.quotableapp.data.common.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun <V, DE, RE : Throwable> MutableStateFlow<UiState<V, DE>>.handleRequest(
    coroutineScope: CoroutineScope,
    requestFunc: suspend () -> Resource<V, RE>,
    errorConverter: (RE) -> (DE)
) {
    coroutineScope.launch {
        this@handleRequest.setLoading()
        val response = requestFunc()
        response.onSuccess {
            this@handleRequest.setData(it)
        }.onFailure {
            this@handleRequest.setError(errorConverter(it))
        }
    }
}

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