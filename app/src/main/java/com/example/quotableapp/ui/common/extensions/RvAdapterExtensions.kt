package com.example.quotableapp.ui.common.extensions

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map

data class RecyclerViewComposite(
    val recyclerView: RecyclerView,
    val emptyListLayout: View? = null,
    val errorLayout: View? = null,
    val swipeRefreshLayout: SwipeRefreshLayout? = null,
    val loadingLayout: View? = null,
    val retryView: View? = null,
)

@FlowPreview
fun <T : PagingDataAdapter<*, *>> T.setupWith(
    recyclerViewComposite: RecyclerViewComposite,
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    onError: ((Throwable) -> Unit)? = null
) {
    val pagingAdapter = this
    recyclerViewComposite.swipeRefreshLayout
        ?.setOnRefreshListener { pagingAdapter.refresh() }
    recyclerViewComposite.retryView
        ?.setOnClickListener { pagingAdapter.refresh() }

    lifecycleCoroutineScope.launchWhenStarted {
        pagingAdapter.loadStateFlow
            .debounce(100)
            .map { it.refresh }
            .collectLatest { refreshState ->
                recyclerViewComposite.updateVisibilities(refreshState, pagingAdapter)
                if (refreshState.isError() && pagingAdapter.itemCount != 0) {
                    onError?.invoke((refreshState as LoadState.Error).error)
                }
            }
    }
}

private fun <T : PagingDataAdapter<*, *>> RecyclerViewComposite.updateVisibilities(
    refreshState: LoadState,
    pagingAdapter: T
) {
    recyclerView.isVisible = isRecyclerViewVisible(
        refreshState = refreshState,
        pagingAdapter = pagingAdapter
    )
    emptyListLayout?.isVisible = isEmptyListLayoutVisible(
        refreshState = refreshState,
        pagingAdapter = pagingAdapter
    )
    errorLayout?.isVisible = isErrorLayoutVisible(
        refreshState = refreshState,
        pagingAdapter = pagingAdapter
    )
    swipeRefreshLayout?.isRefreshing = isSwipeRefreshLayoutVisible(
        refreshState = refreshState,
        pagingAdapter = pagingAdapter
    )
    loadingLayout?.isVisible = isLoadingLayoutVisible(
        refreshState = refreshState,
        pagingAdapter = pagingAdapter
    )
}

private fun isRecyclerViewVisible(
    refreshState: LoadState,
    pagingAdapter: PagingDataAdapter<*, *>
): Boolean {
    return refreshState is LoadState.NotLoading
            || pagingAdapter.itemCount != 0
}

private fun isEmptyListLayoutVisible(
    refreshState: LoadState,
    pagingAdapter: PagingDataAdapter<*, *>
): Boolean {
    return refreshState is LoadState.NotLoading
            && pagingAdapter.itemCount == 0
}

private fun isErrorLayoutVisible(
    refreshState: LoadState,
    pagingAdapter: PagingDataAdapter<*, *>
): Boolean {
    return refreshState.isError()
            && pagingAdapter.itemCount == 0
}

private fun isSwipeRefreshLayoutVisible(
    refreshState: LoadState,
    pagingAdapter: PagingDataAdapter<*, *>
): Boolean {
    return refreshState is LoadState.Loading
}

private fun isLoadingLayoutVisible(
    refreshState: LoadState,
    pagingAdapter: PagingDataAdapter<*, *>
): Boolean {
    return refreshState is LoadState.Loading
            && pagingAdapter.itemCount == 0
}

private fun LoadState.isError() = this is LoadState.Error
