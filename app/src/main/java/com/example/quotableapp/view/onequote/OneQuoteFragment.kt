package com.example.quotableapp.view.onequote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.quotableapp.databinding.OneQuoteFragmentBinding
import com.example.quotableapp.view.common.rvAdapters.TagsAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OneQuoteFragment : Fragment() {

    object StateHandler {

        fun getDataOrNull(state: OneQuoteViewModel.State?): OneQuoteViewModel.State.Data? =
            state as? OneQuoteViewModel.State.Data
    }

    private val viewModel: OneQuoteViewModel by viewModels()

    private lateinit var binding: OneQuoteFragmentBinding

    private val tagsAdapter = TagsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OneQuoteFragmentBinding.inflate(inflater).apply {
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
    }

    private fun handle(state: OneQuoteViewModel.State) {
        // TODO
        when (state) {
            is OneQuoteViewModel.State.Data -> handleValidData(state)
        }
    }

    private fun handle(action: OneQuoteViewModel.Action) {
        // TODO
        when (action) {
            is OneQuoteViewModel.Action.ShowError -> showErrorToast()
        }
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