package com.example.quotableapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quotableapp.databinding.ActivityMainBinding
import com.example.quotableapp.quotesadapter.QuotesAdapter
import com.example.quotableapp.quotesadapter.QuotesLoadingAdapter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@FlowPreview
@ExperimentalTime
@ExperimentalPagingApi
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