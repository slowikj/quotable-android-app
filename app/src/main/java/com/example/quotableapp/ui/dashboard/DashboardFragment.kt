package com.example.quotableapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.databinding.DashboardRecyclerViewItemBinding
import com.example.quotableapp.databinding.FragmentDashboardBinding
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.extensions.handle
import com.example.quotableapp.ui.dashboard.adapters.AuthorsDashboardAdapter
import com.example.quotableapp.ui.dashboard.adapters.QuotesDashboardAdapter
import com.example.quotableapp.ui.dashboard.adapters.DashboardTagsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    private val viewModel: DashboardViewModel by viewModels()

    private val authorsAdapter by lazy {
        AuthorsDashboardAdapter(onClick = { showAuthor(it.slug) })
    }

    private val quotesAdapter: QuotesDashboardAdapter by lazy {
        QuotesDashboardAdapter(onClick = { showQuote(it.id) })
    }

    private val tagsAdapter: DashboardTagsAdapter by lazy {
        DashboardTagsAdapter(onClick = { showQuotesOfTag(it) })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@DashboardFragment.viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategories()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.quotes.collectLatest { handle(it) } }
                launch { viewModel.authors.collectLatest { handle(it) } }
                launch { viewModel.tags.collectLatest { handle(it) } }
                launch { viewModel.randomQuote.collectLatest { handle(it) } }
            }
        }
        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.refreshAll()
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    @JvmName("handleQuoteListState")
    private fun handle(quotesState: UiState<List<Quote>, DashboardViewModel.UiError>) {
        binding.rowQuotes.handleUiState(quotesState)
    }

    @JvmName("handleAuthorListState")
    private fun handle(authorsState: UiState<List<Author>, DashboardViewModel.UiError>) {
        binding.rowAuthors.handleUiState(authorsState)
    }

    @JvmName("handleTagsListState")
    private fun handle(tagsState: UiState<List<Tag>, DashboardViewModel.UiError>) {
        binding.rowTags.handleUiState(tagsState)
    }

    @JvmName("handleRandomQuote")
    private fun handle(randomQuoteState: UiState<Quote, DashboardViewModel.UiError>) {
        with(binding.rowRandomQuote) {
            dataLoadHandler.handle(randomQuoteState)
            this.dataGroup.isVisible = randomQuoteState.data != null && !randomQuoteState.isLoading
            this.model = randomQuoteState.data
        }
    }

    private fun showQuote(quoteId: String) {
        val action = DashboardFragmentDirections.showOneQuote(quoteId)
        findNavController().navigate(action)
    }

    private fun showAuthor(authorSlug: String) {
        val action = DashboardFragmentDirections.showAuthor(authorSlug)
        findNavController().navigate(action)
    }

    private fun showAllQuotes() {
        val action = DashboardFragmentDirections.showAllQuotes()
        findNavController().navigate(action)
    }

    private fun showAllAuthors() {
        val action = DashboardFragmentDirections.showAllAuthors()
        findNavController().navigate(action)
    }

    private fun showQuotesOfTag(tag: Tag) {
        val action = DashboardFragmentDirections.showQuotesOfTag(tag.name)
        findNavController().navigate(action)
    }

    private fun showAllTags() {
        val action = DashboardFragmentDirections.showAllTags()
        findNavController().navigate(action)
    }

    private fun <M> DashboardRecyclerViewItemBinding.handleUiState(state: UiState<List<M>, DashboardViewModel.UiError>) {
        dataLoadHandler.handle(state)
        rvItems.isVisible = state.data != null && !state.isLoading
        (rvItems.adapter as? ListAdapter<M, *>)?.submitList(state.data)
    }

    private fun setupCategories() {
        setupRandomQuoteEntry()
        setupAuthorsEntry()
        setupQuotesEntry()
        setupTagsEntry()
    }

    private fun setupRandomQuoteEntry() {
        with(binding.rowRandomQuote) {
            dataLoadHandler.btnRetry.setOnClickListener {
                viewModel.requestRandomQuote()
            }
            root.setOnClickListener {
                viewModel.randomQuote.value.data?.let { showQuote(it.id) }
            }
            btnRefresh.setOnClickListener {
                viewModel.requestRandomQuote()
            }
        }
    }

    private fun setupTagsEntry() {
        setupCategoryEntry(
            binding = binding.rowTags,
            listAdapter = tagsAdapter,
            onCategoryClickListener = { showAllTags() },
            onDataRetryRequest = { viewModel.updateTags() }
        )
    }

    private fun setupAuthorsEntry() {
        setupCategoryEntry(
            binding = binding.rowAuthors,
            listAdapter = authorsAdapter,
            onCategoryClickListener = { showAllAuthors() },
            onDataRetryRequest = { viewModel.requestAuthors() })
    }

    private fun setupQuotesEntry() {
        setupCategoryEntry(
            binding = binding.rowQuotes,
            listAdapter = quotesAdapter,
            onCategoryClickListener = { showAllQuotes() },
            onDataRetryRequest = { viewModel.requestQuotes() })
    }

    private fun <M, VH : RecyclerView.ViewHolder> setupCategoryEntry(
        binding: DashboardRecyclerViewItemBinding, listAdapter: ListAdapter<M, VH>,
        onCategoryClickListener: () -> Unit,
        onDataRetryRequest: () -> Unit
    ) {
        binding.rvItems.adapter = listAdapter
        binding.headerLayout.root.setOnClickListener { onCategoryClickListener() }
        binding.dataLoadHandler.btnRetry.setOnClickListener { onDataRetryRequest() }
    }

}