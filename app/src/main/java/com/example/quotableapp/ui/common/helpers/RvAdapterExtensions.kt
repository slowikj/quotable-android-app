package com.example.quotableapp.ui.common.helpers

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quotableapp.data.network.common.HttpApiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

fun <T : PagingDataAdapter<*, *>> T.handleRefreshing(
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    swipeRefreshLayout: SwipeRefreshLayout,
    onError: (Throwable) -> Unit
) {
    val pagingAdapter = this
    lifecycleCoroutineScope.launchWhenStarted {
        pagingAdapter
            .loadStateFlow
            .debounce(100)
            .collectLatest { loadStates ->
                handleLoadStates(loadStates, swipeRefreshLayout, onError)
            }
    }
}

private fun handleLoadStates(
    loadStates: CombinedLoadStates,
    swipeRefreshLayout: SwipeRefreshLayout,
    onError: (Throwable) -> Unit
) {
    val refreshState = loadStates.refresh
    swipeRefreshLayout.isRefreshing =
        refreshState is LoadState.Loading

    if (refreshState is LoadState.Error
        && refreshState.error !is HttpApiError.CancelledRequest
    ) {
        onError(refreshState.error)
    }
}

fun <T : PagingDataAdapter<*, *>> T.handleEmptyList(
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    recyclerView: RecyclerView,
    emptyListLayout: ViewGroup
) {
    val pagingAdapter = this
    lifecycleCoroutineScope.launchWhenStarted {
        pagingAdapter
            .loadStateFlow
            .debounce(100)
            .collectLatest { loadStates ->
                val refreshState = loadStates.refresh
                val isEmpty = refreshState !is LoadState.Loading
                        && pagingAdapter.itemCount == 0
                recyclerView.isVisible = !isEmpty
                emptyListLayout.isVisible = isEmpty
            }
    }
}