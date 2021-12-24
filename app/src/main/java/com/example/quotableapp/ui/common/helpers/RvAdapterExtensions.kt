package com.example.quotableapp.ui.common.helpers

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.flow.collectLatest

fun <T : PagingDataAdapter<*, *>> T.handleRefreshing(
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    swipeRefreshLayout: SwipeRefreshLayout,
    onError: (Throwable) -> Unit
) {
    val pagingAdapter = this
    lifecycleCoroutineScope.launchWhenStarted {
        pagingAdapter
            .loadStateFlow
            .collectLatest { loadStates ->
                swipeRefreshLayout.isRefreshing =
                    loadStates.refresh is LoadState.Loading

                if (loadStates.refresh is LoadState.Error) {
                    onError((loadStates.refresh as LoadState.Error).error)
                }
            }
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
            .collectLatest { loadStates ->
                val isEmpty =
                    loadStates.refresh !is LoadState.Loading && pagingAdapter.itemCount == 0
                recyclerView.isVisible = !isEmpty
                emptyListLayout.isVisible = isEmpty
            }
    }
}