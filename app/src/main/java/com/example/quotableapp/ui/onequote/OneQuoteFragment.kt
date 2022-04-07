package com.example.quotableapp.ui.onequote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.quotableapp.databinding.FragmentOneQuoteBinding
import com.example.quotableapp.ui.common.extensions.copyQuoteToClipBoardWithToast
import com.example.quotableapp.ui.common.extensions.handle
import com.example.quotableapp.ui.common.extensions.showErrorToast
import com.example.quotableapp.ui.common.extensions.showToast
import com.example.quotableapp.ui.common.rvAdapters.QuoteTagsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OneQuoteFragment : Fragment() {

    private val viewModel: OneQuoteViewModel by viewModels()

    private lateinit var binding: FragmentOneQuoteBinding

    private val tagsAdapter = QuoteTagsAdapter(onClick = { showQuotesOfTag(it) })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOneQuoteBinding.inflate(inflater, container, false).apply {
            viewModel = this@OneQuoteFragment.viewModel
            lifecycleOwner = this@OneQuoteFragment.viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setObservingViewModel()
    }

    private fun setupUi() {
        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            viewModel.updateQuoteUi()
        }
        binding.quoteLayout.rvTags.adapter = tagsAdapter
        binding.dataLoadHandler.btnRetry.setOnClickListener { viewModel.updateQuoteUi() }
    }

    private fun setObservingViewModel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.quoteUiState.collectLatest { handle(it) }
            }
        }
    }

    private fun handle(state: QuoteUiState) {
        binding.dataLoadHandler.handle(state)
        if (state.data != null) {
            val quote = state.data
            tagsAdapter.submitList(quote.tags)
            setupListeners(quote)
        }
        if (state.error is OneQuoteViewModel.UiError.IOError) {
            showErrorToast()
        }
        state.error?.let {
            viewModel.onErrorConsumed(it)
        }
    }

    private fun setupListeners(quote: QuoteUi) {
        with(binding.quoteLayout) {
            ivAuthor.setOnClickListener { showAuthorFragment(quote.authorSlug) }
            tvAuthor.setOnClickListener { showAuthorFragment(quote.authorSlug) }
            btnShare.setOnClickListener { onShareClick(quote) }
            btnLike.setOnClickListener { onLikeClick(quote) }
            btnCopy.setOnClickListener { onCopyClick(quote) }
        }
    }

    private fun showQuotesOfTag(tag: String) {
        val action = OneQuoteFragmentDirections.showQuotesOfTag(tag)
        findNavController().navigate(action)
    }

    private fun showAuthorFragment(authorSlug: String) {
        val action = OneQuoteFragmentDirections.showAuthor(authorSlug)
        findNavController().navigate(action)
    }

    private fun onCopyClick(quote: QuoteUi) {
        copyQuoteToClipBoardWithToast(quote.formattedText)
    }

    private fun onLikeClick(quote: QuoteUi) {
        showToast("TODO: Will be implemented soon!")
    }

    private fun onShareClick(quote: QuoteUi) {
        showToast("TODO: Will be implemented soon!")
    }
}