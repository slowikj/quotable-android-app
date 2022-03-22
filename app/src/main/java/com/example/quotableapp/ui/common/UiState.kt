package com.example.quotableapp.ui.common

import com.example.quotableapp.ui.common.extensions.defaultSharingStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState<V, E>(
    val data: V? = null,
    val isLoading: Boolean = false,
    val error: E? = null
) {
}

class UiStateManager<V, E>(
    private val coroutineScope: CoroutineScope,
    private val sourceDataFlow: Flow<V>,
    private val initIsLoading: Boolean = false,
    private val initError: E? = null,
    private val initData: V? = null
) {
    private val initState: UiState<V, E> = UiState(
        data = initData,
        isLoading = initIsLoading,
        error = initError
    )

    val dataFlow: StateFlow<V?> = sourceDataFlow
        .stateIn(
            initialValue = initData,
            scope = coroutineScope,
            started = defaultSharingStarted
        )

    val isLoadingFlow = MutableStateFlow<Boolean>(initIsLoading)

    val errorFlow = MutableStateFlow<E?>(initError)

    val stateFlow: StateFlow<UiState<V, E>> =
        combine(dataFlow, isLoadingFlow, errorFlow) { data, isLoading, error ->
            UiState(data = data, isLoading = isLoading, error = error)
        }.stateIn(
            initialValue = initState,
            scope = coroutineScope,
            started = defaultSharingStarted
        )

    fun updateData(
        requestFunc: suspend () -> Result<Unit>,
        errorTransformer: (Throwable) -> E
    ) {
        coroutineScope.launch {
            isLoadingFlow.value = true
            val response = requestFunc()
            errorFlow.value = response.exceptionOrNull()?.let { errorTransformer(it) }
            isLoadingFlow.value = false
        }
    }
}
