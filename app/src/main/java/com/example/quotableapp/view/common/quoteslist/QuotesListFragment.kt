package com.example.quotableapp.view.common.quoteslist

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quotableapp.R
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.RefreshableRecyclerviewBinding
import com.example.quotableapp.view.common.quoteslist.quotesadapter.QuotesAdapter
import com.example.quotableapp.view.common.quoteslist.quotesadapter.QuotesLoadingAdapter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlin.time.ExperimentalTime

@FlowPreview
@ExperimentalTime
@ExperimentalPagingApi
abstract class QuotesListFragment<ListViewModelType : QuotesListViewModel> : Fragment() {

    protected abstract val listViewModel: ListViewModelType

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

    protected abstract val recyclerViewLayoutBinding: RefreshableRecyclerviewBinding

    protected val swipeToRefresh: SwipeRefreshLayout
        get() = recyclerViewLayoutBinding.swipeToRefresh

    protected val rvQuotes: RecyclerView
        get() = recyclerViewLayoutBinding.rvQuotes

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuotesAdapter()
        setupPullToRefresh()
        setupActionHandler()
    }

    private fun setupActionHandler() {
        listViewModel.actions.observe(viewLifecycleOwner) {
            when (it) {
                is QuotesListViewModel.Action.Navigation -> handleNavigation(it)
                is QuotesListViewModel.Action.Error -> showErrorToast()
                is QuotesListViewModel.Action.CopyToClipboard -> TODO()
                is QuotesListViewModel.Action.InvalidateQuotes -> quotesAdapter.refresh()
            }
        }
    }

    private fun handleNavigation(action: QuotesListViewModel.Action.Navigation) {
        when (action) {
            is QuotesListViewModel.Action.Navigation.ToQuotesOfAuthor -> showAuthorFragment(action.authorSlug)
            is QuotesListViewModel.Action.Navigation.ToDetails -> showQuote(action.quote)
            is QuotesListViewModel.Action.Navigation.ToQuotesOfTag -> showQuotesOfTag(action.tag)
        }
    }

    private fun setupQuotesAdapter() {
        rvQuotes.adapter = quotesAdapter.withLoadStateFooter(
            footer = QuotesLoadingAdapter { quotesAdapter.retry() }
        )

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            listViewModel.fetchQuotes().collectLatest {
                quotesAdapter.submitData(it)
            }
        }
    }

    private fun setupPullToRefresh() {
        swipeToRefresh.setOnRefreshListener { listViewModel.onRefresh() }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            whenStarted {
                quotesAdapter
                    .loadStateFlow
                    .distinctUntilChangedBy { it.refresh }
                    .collectLatest { loadStates ->
                        swipeToRefresh.isRefreshing =
                            loadStates.refresh is LoadState.Loading

                        if (loadStates.refresh is LoadState.Error) {
                            showErrorToast()
                        }
                    }
            }
        }
    }

    protected abstract fun showQuote(quote: Quote)

    protected abstract fun showAuthorFragment(authorSlug: String)

    protected abstract fun showQuotesOfTag(tag: String)

    private fun showErrorToast() {
        Toast.makeText(
            context,
            getString(R.string.error_occurred),
            Toast.LENGTH_SHORT
        ).show()
    }
}