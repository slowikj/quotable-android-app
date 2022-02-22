package com.example.quotableapp.ui.common.quoteslist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.RefreshableRecyclerviewBinding
import com.example.quotableapp.ui.common.extensions.RecyclerViewComposite
import com.example.quotableapp.ui.common.extensions.copyQuoteToClipBoardWithToast
import com.example.quotableapp.ui.common.extensions.setupWith
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

    protected lateinit var recyclerViewComposite: RecyclerViewComposite

    private val quotesAdapter by lazy { QuotesAdapter(onClickHandler = listViewModel) }

    protected abstract fun showQuote(quote: Quote)

    protected abstract fun showAuthorFragment(authorSlug: String)

    protected abstract fun showQuotesOfTag(tag: String)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewComposite = prepareRecyclerViewComposite()
        setupQuotesRecyclerView()
        setupQuotesAdapter()
        setupActionsHandler()
        recyclerViewLayoutBinding.dataLoadHandler.btnRetry.setOnClickListener {
            listViewModel.onRefresh()
        }
    }

    private fun prepareRecyclerViewComposite() = RecyclerViewComposite(
        recyclerView = recyclerViewLayoutBinding.rvQuotes,
        emptyListLayout = recyclerViewLayoutBinding.emptyListLayout.root,
        errorLayout = recyclerViewLayoutBinding.dataLoadHandler.errorHandler,
        swipeRefreshLayout = recyclerViewLayoutBinding.swipeToRefresh,
        loadingLayout = recyclerViewLayoutBinding.dataLoadHandler.progressBar
    )

    private fun setupQuotesAdapter() {
        recyclerViewComposite.swipeRefreshLayout
            ?.setOnRefreshListener { listViewModel.onRefresh() }
        quotesAdapter.setupWith(
            recyclerViewComposite = recyclerViewComposite,
            lifecycleCoroutineScope = viewLifecycleOwner.lifecycleScope,
            onError = { showErrorToast() }
        )
    }

    private fun setupQuotesRecyclerView() {
        recyclerViewComposite.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = quotesAdapter.withLoadStateFooter(
                footer = DefaultLoadingAdapter { quotesAdapter.retry() }
            )
        }
    }

    private fun setupActionsHandler() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectQuotesFlow() }
                launch { collectNavigationActions() }
                launch { collectPlainActions() }
            }
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
            is QuotesListViewModel.Action.CopyToClipboard -> copyQuoteToClipBoardWithToast(action.formattedQuote)
            is QuotesListViewModel.Action.RefreshQuotes -> quotesAdapter.refresh()
        }

    private fun handleNavigation(action: QuotesListViewModel.NavigationAction) =
        when (action) {
            is QuotesListViewModel.NavigationAction.ToQuotesOfAuthor -> showAuthorFragment(action.authorSlug)
            is QuotesListViewModel.NavigationAction.ToDetails -> showQuote(action.quote)
            is QuotesListViewModel.NavigationAction.ToQuotesOfTag -> showQuotesOfTag(action.tag)
        }
}