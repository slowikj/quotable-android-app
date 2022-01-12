package com.example.quotableapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.DashboardRecyclerViewItemBinding
import com.example.quotableapp.databinding.FragmentDashboardBinding
import com.example.quotableapp.ui.common.uistate.UiState
import com.example.quotableapp.ui.dashboard.adapters.AuthorsAdapter
import com.example.quotableapp.ui.dashboard.adapters.QuotesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    private val viewModel: DashboardViewModel by viewModels()

    private val authorsAdapter by lazy {
        AuthorsAdapter(onClick = { viewModel.onAuthorClick(it) })
    }

    private val quotesAdapter: QuotesAdapter by lazy {
        QuotesAdapter(onClick = { viewModel.onQuoteClick(it) })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater).apply {
            lifecycleOwner = this@DashboardFragment.viewLifecycleOwner
        }
        setupCategories()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { viewModel.quotes.collectLatest { handle(it) } }
            launch { viewModel.authors.collectLatest { handle(it) } }
            launch { viewModel.navigationActions.collectLatest { handle(it) } }
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

    @JvmName("handleNavigationAction")
    private fun handle(navigationAction: DashboardViewModel.NavigationAction) =
        when (navigationAction) {
            is DashboardViewModel.NavigationAction.ToQuote -> showQuote(navigationAction.quoteId)
            is DashboardViewModel.NavigationAction.ToAuthor -> showAuthor(navigationAction.authorSlug)
            is DashboardViewModel.NavigationAction.ToAllQuotes -> showAllQuotes()
            is DashboardViewModel.NavigationAction.ToAllAuthors -> showAllAuthors()
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

    private fun <M> DashboardRecyclerViewItemBinding.handleUiState(state: UiState<List<M>, DashboardViewModel.UiError>) {
        tvError.isVisible = state.error != null && !state.isLoading
        btnRetry.isVisible = state.error != null && !state.isLoading
        headerLayout.ivSeeMore.isVisible = state.error == null
        progressBar.isVisible = state.isLoading
        rvItems.isVisible = state.data != null
        (rvItems.adapter as? ListAdapter<M, *>)?.submitList(state.data)
    }

    private fun setupCategories() {
        setupAuthorsEntry()
        setupQuotesEntry()
    }

    private fun setupAuthorsEntry() {
        setupCategoryEntry(
            binding = binding.rowAuthors,
            listAdapter = authorsAdapter,
            onCategoryClickListener = { viewModel.onAuthorsShowMoreClick() },
            onDataRetryRequest = { viewModel.requestAuthors() })
    }

    private fun setupQuotesEntry() {
        setupCategoryEntry(
            binding = binding.rowQuotes,
            listAdapter = quotesAdapter,
            onCategoryClickListener = { viewModel.onQuotesShowMoreClick() },
            onDataRetryRequest = { viewModel.requestQuotes() })
    }

    private fun <M, VH : RecyclerView.ViewHolder> setupCategoryEntry(
        binding: DashboardRecyclerViewItemBinding, listAdapter: ListAdapter<M, VH>,
        onCategoryClickListener: () -> Unit,
        onDataRetryRequest: () -> Unit
    ) {
        binding.rvItems.adapter = listAdapter
        binding.headerLayout.root.setOnClickListener { onCategoryClickListener() }
        binding.btnRetry.setOnClickListener { onDataRetryRequest() }
    }

}