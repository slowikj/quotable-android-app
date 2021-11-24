package com.example.quotableapp.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import com.example.quotableapp.R
import com.example.quotableapp.databinding.ActivityMainBinding
import com.example.quotableapp.view.quotesadapter.QuotesAdapter
import com.example.quotableapp.view.quotesadapter.QuotesLoadingAdapter
import com.example.quotableapp.viewmodel.QuotesViewModel
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
class MainActivity : AppCompatActivity() {

    private val viewModel: QuotesViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    private val quotesAdapter = QuotesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupQuotesAdapter()
        setupPullToRefresh()
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupQuotesAdapter() {
        binding.rvQuotes.adapter = quotesAdapter.withLoadStateFooter(
            footer = QuotesLoadingAdapter { quotesAdapter.retry() }
        )

        lifecycleScope.launch {
            viewModel.fetchQuotes().collectLatest {
                quotesAdapter.submitData(it)
            }
        }
    }

    private fun setupPullToRefresh() {
        binding.swipeToRefresh.setOnRefreshListener { quotesAdapter.refresh() }

        lifecycleScope.launchWhenCreated {
            quotesAdapter
                .loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .collectLatest { loadStates ->
                    binding.swipeToRefresh.isRefreshing = loadStates.refresh is LoadState.Loading

                    if (loadStates.refresh is LoadState.Error) {
                        showErrorToast()
                    }
                }
        }
    }

    private fun showErrorToast() {
        Toast.makeText(
            applicationContext,
            getString(R.string.error_occurred),
            Toast.LENGTH_SHORT
        ).show()
    }
}