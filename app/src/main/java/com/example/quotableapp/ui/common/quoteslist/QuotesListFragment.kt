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
import com.example.quotableapp.ui.common.OnQuoteClickListener
import com.example.quotableapp.ui.common.extensions.RecyclerViewComposite
import com.example.quotableapp.ui.common.extensions.copyQuoteToClipBoardWithToast
import com.example.quotableapp.ui.common.extensions.setupWith
import com.example.quotableapp.ui.common.extensions.showErrorToast
import com.example.quotableapp.ui.common.formatters.formatToClipboard
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

    private val quotesAdapter by lazy {
        QuotesAdapter(onClickHandler = object : OnQuoteClickListener {
            override fun onItemClick(quote: Quote) {
                showQuote(quote)
            }

            override fun onItemLongClick(quote: Quote): Boolean {
                copyQuoteToClipBoardWithToast(quote.formatToClipboard())
                return true
            }

            override fun onAuthorClick(authorSlug: String) {
                showAuthorFragment(authorSlug)
            }

            override fun onTagClick(tag: String) {
                showQuotesOfTag(tag)
            }
        })
    }

    protected abstract fun showQuote(quote: Quote)

    protected abstract fun showAuthorFragment(authorSlug: String)

    protected abstract fun showQuotesOfTag(tag: String)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewComposite = prepareRecyclerViewComposite()
        setupQuotesRecyclerView()
        setupQuotesAdapter()
        setupFlowsHandler()
        recyclerViewLayoutBinding.dataLoadHandler.btnRetry.setOnClickListener {
            quotesAdapter.refresh()
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
            ?.setOnRefreshListener { quotesAdapter.refresh() }
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

    private fun setupFlowsHandler() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectQuotesFlow() }
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

    private fun handlePlainActions(action: QuotesListViewModel.Action) =
        when (action) {
            is QuotesListViewModel.Action.Error -> showErrorToast()
        }
}
