package com.example.quotableapp.ui.common.extensions

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.databinding.RefreshableRecyclerviewBinding
import com.example.quotableapp.ui.common.rvAdapters.DefaultLoadingAdapter

fun RefreshableRecyclerviewBinding.prepareComposite() = RecyclerViewComposite(
    recyclerView = rvQuotes,
    emptyListLayout = emptyListLayout.root,
    errorLayout = dataLoadHandler.errorHandler,
    swipeRefreshLayout = swipeToRefresh,
    loadingLayout = dataLoadHandler.progressBar,
    retryView = dataLoadHandler.btnRetry
)

fun <T : PagingDataAdapter<*, *>> RecyclerView.setUpLinearWithFooter(pagingAdapter: T) {
    layoutManager = LinearLayoutManager(context)
    adapter = pagingAdapter.withLoadStateFooter(
        footer = DefaultLoadingAdapter { pagingAdapter.retry() }
    )
}
