package com.example.quotableapp.view.allquotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import com.example.quotableapp.R
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.FragmentAllQuotesBinding
import com.example.quotableapp.view.quotesadapter.QuotesAdapter
import com.example.quotableapp.view.quotesadapter.QuotesLoadingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@FlowPreview
@ExperimentalTime
@ExperimentalPagingApi
@AndroidEntryPoint
class AllQuotesFragment : Fragment() {

    private val viewModelAll: AllQuotesViewModel by viewModels()

    private lateinit var binding: FragmentAllQuotesBinding

    private val quotesAdapter = QuotesAdapter(onItemClick = { showQuote(it) })

    private fun showQuote(quote: Quote) {
        val action = AllQuotesFragmentDirections.showOneQuote(quote.id)
        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllQuotesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuotesAdapter()
        setupPullToRefresh()
    }

    private fun setupQuotesAdapter() {
        binding.rvQuotes.adapter = quotesAdapter.withLoadStateFooter(
            footer = QuotesLoadingAdapter { quotesAdapter.retry() }
        )

        lifecycleScope.launch {
            viewModelAll.fetchQuotes().collectLatest {
                quotesAdapter.submitData(it)
            }
        }
    }

    private fun setupPullToRefresh() {
        binding.swipeToRefresh.setOnRefreshListener { quotesAdapter.refresh() }

        lifecycleScope.launch {
            whenStarted {
                quotesAdapter
                    .loadStateFlow
                    .distinctUntilChangedBy { it.refresh }
                    .collectLatest { loadStates ->
                        binding.swipeToRefresh.isRefreshing =
                            loadStates.refresh is LoadState.Loading

                        if (loadStates.refresh is LoadState.Error) {
                            showErrorToast()
                        }
                    }
            }
        }
    }

    private fun showErrorToast() {
        Toast.makeText(
            context,
            getString(R.string.error_occurred),
            Toast.LENGTH_SHORT
        ).show()
    }
}