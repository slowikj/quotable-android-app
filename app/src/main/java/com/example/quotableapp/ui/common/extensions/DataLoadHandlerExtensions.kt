package com.example.quotableapp.ui.common.extensions

import androidx.core.view.isVisible
import com.example.quotableapp.databinding.LoadDataHandlerLineBinding
import com.example.quotableapp.ui.common.UiState

fun LoadDataHandlerLineBinding.handle(state: UiState<*, *>) {
    tvError.isVisible = state.error != null && !state.isLoading
    btnRetry.isVisible = state.error != null && !state.isLoading
    progressBar.isVisible = state.isLoading
}