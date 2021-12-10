package com.example.quotableapp.view.onequote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.quotableapp.databinding.FragmentOneQuoteBinding
import com.example.quotableapp.view.common.TagsAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OneQuoteFragment : Fragment() {

    object StateHandler {

        fun getDataOrNull(state: OneQuoteViewModel.State?): OneQuoteViewModel.State.Data? =
            state as? OneQuoteViewModel.State.Data
    }

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
            stateHandler = StateHandler
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.quoteLayout.rvTags.adapter = tagsAdapter
        viewModel.state.observe(viewLifecycleOwner) { handle(it) }
        viewModel.action.observe(viewLifecycleOwner) { handle(it) }

        binding.quoteLayout.apply {
            author.setOnClickListener { viewModel.onAuthorClick() }
            letterIcon.setOnClickListener { viewModel.onAuthorClick() }
        }
    }

    private fun handle(state: OneQuoteViewModel.State) {
        // TODO
        when (state) {
            is OneQuoteViewModel.State.Data -> handleValidData(state)
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

    private fun handleValidData(state: OneQuoteViewModel.State.Data) {
        binding.quoteLayout.letterIcon.letter =
            if (state.quote.author.isNotEmpty()) state.quote.author[0].toString() else "?"
        tagsAdapter.submitList(state.quote.tags)
    }

    private fun showErrorToast() {
        Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}