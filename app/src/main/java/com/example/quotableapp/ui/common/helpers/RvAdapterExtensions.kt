package com.example.quotableapp.ui.common.helpers

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy

fun <T : PagingDataAdapter<*, *>> T.handleRefreshing(
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    swipeRefreshLayout: SwipeRefreshLayout,
    onError: (Throwable) -> Unit
) {
    lifecycleCoroutineScope.launchWhenStarted {
        this@handleRefreshing.loadStateFlow
            .distinctUntilChangedBy { it.refresh }
            .collectLatest { loadStates ->
                swipeRefreshLayout.isRefreshing =
                    loadStates.refresh is LoadState.Loading

                if (loadStates.refresh is LoadState.Error) {
                    onError((loadStates.refresh as LoadState.Error).error)
                }
            }
    }
}
