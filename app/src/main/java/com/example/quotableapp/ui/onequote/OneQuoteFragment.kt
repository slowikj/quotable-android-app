package com.example.quotableapp.ui.onequote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quotableapp.databinding.FragmentOneQuoteBinding
import com.example.quotableapp.ui.common.extensions.copyQuoteToClipBoardWithToast
import com.example.quotableapp.ui.common.extensions.copyToClipBoardWithToast
import com.example.quotableapp.ui.common.extensions.handle
import com.example.quotableapp.ui.common.extensions.showErrorToast
import com.example.quotableapp.ui.common.rvAdapters.TagsAdapter
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
        setupUi()
        setObservingViewModel()
    }

    private fun setupUi() {
        with(binding.quoteLayout) {
            author.setOnClickListener { viewModel.onAuthorClick() }
            root.setOnLongClickListener { viewModel.onQuoteLongClick() }
            rvTags.adapter = tagsAdapter
        }
        binding.dataLoadHandler.btnRetry.setOnClickListener { viewModel.requestData() }
    }

    private fun setObservingViewModel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch { viewModel.state.collectLatest { handle(it) } }
            launch { viewModel.action.collect { handle(it) } }
        }
    }

    private fun handle(state: OneQuoteUiState) {
        binding.dataLoadHandler.handle(state)
        tagsAdapter.submitList(state.data?.tags)
    }

    private fun handle(action: OneQuoteViewModel.Action) {
        when (action) {
            is OneQuoteViewModel.Action.ShowError -> showErrorToast()
            is OneQuoteViewModel.Action.Navigation.ToAuthorQuotes -> showAuthorFragment(action.authorSlug)
            is OneQuoteViewModel.Action.Navigation.ToTagQuotes -> showQuotesOfTag(action.tag)
            is OneQuoteViewModel.Action.CopyToClipboard -> copyQuoteToClipBoardWithToast(action.formattedText)
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
}