package com.example.quotableapp.ui.common.extensions

import androidx.core.view.isVisible
import com.example.quotableapp.databinding.LoadDataHandlerLayoutBinding
import com.example.quotableapp.ui.common.UiState

fun LoadDataHandlerLayoutBinding.handle(state: UiState<*, *>) {
    tvError.isVisible = state.run { error != null && !isLoading && data == null }
    btnRetry.isVisible = state.run { error != null && !isLoading && data == null }
    progressBar.isVisible = state.isLoading
}