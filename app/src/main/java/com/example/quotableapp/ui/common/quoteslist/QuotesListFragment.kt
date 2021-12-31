package com.example.quotableapp.ui.common.quoteslist

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.RefreshableRecyclerviewBinding
import com.example.quotableapp.ui.common.extensions.handleEmptyList
import com.example.quotableapp.ui.common.extensions.handleRefreshing
import com.example.quotableapp.ui.common.extensions.showErrorToast
import com.example.quotableapp.ui.common.rvAdapters.DefaultLoadingAdapter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@FlowPreview
@ExperimentalTime
@ExperimentalPagingApi
abstract class QuotesListFragment<ListViewModelType : QuotesListViewModel> : Fragment() {

    protected abstract val listViewModel: ListViewModelType

    protected abstract val recyclerViewLayoutBinding: RefreshableRecyclerviewBinding

    protected val swipeToRefresh: SwipeRefreshLayout
        get() = recyclerViewLayoutBinding.swipeToRefresh

    protected val rvQuotes: RecyclerView
        get() = recyclerViewLayoutBinding.rvQuotes

    protected val emptyListLayout: ViewGroup
        get() = recyclerViewLayoutBinding.emptyListLayout.root

    private val quotesAdapter =
        QuotesAdapter(onClickHandler = object : QuotesAdapter.ViewHolder.OnClickHandler {
            override fun onItem(quote: Quote) {
                listViewModel.onItemClick(quote)
            }

            override fun onAuthor(quote: Quote) {
                listViewModel.onAuthorClick(quote)
            }

            override fun onTag(tag: String) {
                listViewModel.onTagClick(tag)
            }
        })

    protected abstract fun showQuote(quote: Quote)

    protected abstract fun showAuthorFragment(authorSlug: String)

    protected abstract fun showQuotesOfTag(tag: String)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuotesAdapter()
        setupQuotesRecyclerView()
        setupActionsHandler()
    }

    private fun setupQuotesAdapter() {
        setupPullToRefresh()
        quotesAdapter.handleEmptyList(
            lifecycleCoroutineScope = viewLifecycleOwner.lifecycleScope,
            recyclerView = rvQuotes,
            emptyListLayout = emptyListLayout
        )
    }

    private fun setupQuotesRecyclerView() {
        with(rvQuotes) {
            layoutManager = LinearLayoutManager(context)
            adapter = quotesAdapter.withLoadStateFooter(
                footer = DefaultLoadingAdapter { quotesAdapter.retry() }
            )
        }
    }

    private fun setupActionsHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { collectQuotesFlow() }
            launch { collectNavigationActions() }
            launch { collectPlainActions() }
        }
    }

    private suspend fun collectQuotesFlow() {
        listViewModel.quotes.filterNotNull()
            .collectLatest { quotesAdapter.submitData(it) }
    }

    private suspend fun collectPlainActions() {
        listViewModel.actions.collect { handlePlainActions(it) }
    }

    private suspend fun collectNavigationActions() {
        listViewModel.navigationAction.collect { handleNavigation(it) }
    }

    private fun handlePlainActions(action: QuotesListViewModel.Action) =
        when (action) {
            is QuotesListViewModel.Action.Error -> showErrorToast()
            is QuotesListViewModel.Action.CopyToClipboard -> TODO()
            is QuotesListViewModel.Action.RefreshQuotes -> quotesAdapter.refresh()
        }

    private fun handleNavigation(action: QuotesListViewModel.NavigationAction) =
        when (action) {
            is QuotesListViewModel.NavigationAction.ToQuotesOfAuthor -> showAuthorFragment(action.authorSlug)
            is QuotesListViewModel.NavigationAction.ToDetails -> showQuote(action.quote)
            is QuotesListViewModel.NavigationAction.ToQuotesOfTag -> showQuotesOfTag(action.tag)
        }

    private fun setupPullToRefresh() {
        swipeToRefresh.setOnRefreshListener { listViewModel.onRefresh() }
        quotesAdapter.handleRefreshing(
            lifecycleCoroutineScope = viewLifecycleOwner.lifecycleScope,
            swipeRefreshLayout = swipeToRefresh,
            onError = { showErrorToast() }
        )
    }

}