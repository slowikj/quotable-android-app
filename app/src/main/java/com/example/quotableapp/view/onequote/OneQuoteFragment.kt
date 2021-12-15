package com.example.quotableapp.view.onequote

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.FragmentOneQuoteBinding
import com.example.quotableapp.view.common.TagsAdapter
import com.example.quotableapp.view.common.uistate.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class OneQuoteFragment : Fragment() {

    private val viewModel: OneQuoteViewModel by viewModels()

    private lateinit var binding: FragmentOneQuoteBinding

    private val tagsAdapter = TagsAdapter(onClick = { viewModel.onTagClick(it) })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOneQuoteBinding.inflate(inflater).apply {
            viewModel = this@OneQuoteFragment.viewModel
            lifecycleOwner = this@OneQuoteFragment.viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.quoteLayout.rvTags.adapter = tagsAdapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { viewModel.state.collectLatest { handle(it) } }
            launch { viewModel.action.collect { handle(it) } }
        }

        binding.quoteLayout.apply {
            author.setOnClickListener { viewModel.onAuthorClick() }
            letterIcon.setOnClickListener { viewModel.onAuthorClick() }
        }
    }

    private fun handle(state: OneQuoteUiState) {
        if (state.data != null) {
            handleValidData(state.data)
        }
    }

    private fun handle(action: OneQuoteViewModel.Action) {
        when (action) {
            is OneQuoteViewModel.Action.ShowError -> showErrorToast()
            is OneQuoteViewModel.Action.Navigation.ToAuthorQuotes -> showAuthorFragment(action.authorSlug)
            is OneQuoteViewModel.Action.Navigation.ToTagQuotes -> showQuotesOfTag(action.tag)
        }
    }

    private fun showQuotesOfTag(tag: String) {
        val action = OneQuoteFragmentDirections.showTagQuotes(tag)
        findNavController().navigate(action)
    }

    private fun showAuthorFragment(authorSlug: String) {
        val action = OneQuoteFragmentDirections.showAuthor(authorSlug)
        findNavController().navigate(action)
    }

    private fun handleValidData(quote: Quote) {
        binding.quoteLayout.letterIcon.letter =
            if (quote.author.isNotEmpty()) quote.author[0].toString() else "?"
        tagsAdapter.submitList(quote.tags)
    }

    private fun showErrorToast() {
        Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show()
    }
}