package com.example.quotableapp.ui.common.extensions

import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.ui.common.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun <V, PresentationError : Throwable, RepositoryError : Throwable> MutableStateFlow<UiState<V, PresentationError>>.handleRequestWithResult(
    coroutineScope: CoroutineScope,
    requestFunc: suspend () -> Resource<V, RepositoryError>,
    errorConverter: (RepositoryError) -> (PresentationError)
) {
    if (value.isLoading) return
    coroutineScope.launch {
        this@handleRequestWithResult.set(isLoading = true)
        val response = requestFunc()
        response.onSuccess {
            this@handleRequestWithResult.set(data = it, isLoading = false)
        }.onFailure {
            this@handleRequestWithResult.set(error = errorConverter(it), isLoading = false)
        }
    }
}

fun <V, PresentationError : Throwable, RepositoryError : Throwable> MutableStateFlow<UiState<V, PresentationError>>.handleOneShotRequest(
    coroutineScope: CoroutineScope,
    requestFunc: suspend () -> Resource<Boolean, RepositoryError>,
    errorConverter: (RepositoryError) -> (PresentationError)
) {
    if (value.isLoading) return
    coroutineScope.launch {
        this@handleOneShotRequest.set(isLoading = true)
        val response = requestFunc()
        response.onSuccess {
            this@handleOneShotRequest.set(isLoading = false)
        }.onFailure {
            this@handleOneShotRequest.set(error = errorConverter(it), isLoading = false)
        }
    }
}

fun <V, E> MutableStateFlow<UiState<V, E>>.set(
    isLoading: Boolean? = null,
    data: V? = null,
    error: E? = null
) {
    value = value.copy(
        isLoading = isLoading ?: value.isLoading,
        data = data ?: value.data,
        error = error ?: value.error
    )
}
