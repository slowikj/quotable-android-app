package com.example.quotableapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quotableapp.databinding.ActivityMainBinding
import com.example.quotableapp.quotesadapter.QuotesAdapter
import com.example.quotableapp.quotesadapter.QuotesLoadingAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: QuotesViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    private val quotesAdapter = QuotesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvQuotes.adapter = quotesAdapter.withLoadStateHeaderAndFooter(
            header = QuotesLoadingAdapter { quotesAdapter.retry() },
            footer = QuotesLoadingAdapter { quotesAdapter.retry() }
        )

        lifecycleScope.launch {
            viewModel.fetchQuotes().collectLatest {
                quotesAdapter.submitData(it)
            }
        }
    }
}