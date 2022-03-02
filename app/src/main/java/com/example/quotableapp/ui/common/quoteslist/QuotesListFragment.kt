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
import com.example.quotableapp.ui.common.extensions.*
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
abstract class QuotesListFragment : Fragment() {

    protected abstract val quotesProvider: QuotesProvider

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
        recyclerViewComposite = recyclerViewLayoutBinding.prepareComposite()
        setupQuotesRecyclerView()
        setupQuotesAdapter()
        setupFlowsHandler()
    }

    private fun setupQuotesAdapter() {
        quotesAdapter.setupWith(
            recyclerViewComposite = recyclerViewComposite,
            lifecycleCoroutineScope = viewLifecycleOwner.lifecycleScope,
            onError = { showErrorToast() }
        )
    }

    private fun setupQuotesRecyclerView() {
        recyclerViewComposite.recyclerView.setUpLinearWithFooter(quotesAdapter)
    }

    private fun setupFlowsHandler() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectQuotesFlow() }
            }
        }
    }

    private suspend fun collectQuotesFlow() {
        quotesProvider
            .quotes
            .filterNotNull()
            .collectLatest { quotesAdapter.submitData(it) }
    }
}
