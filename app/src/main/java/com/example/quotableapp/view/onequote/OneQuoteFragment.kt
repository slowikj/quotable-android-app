package com.example.quotableapp.view.onequote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.quotableapp.databinding.OneQuoteFragmentBinding
import com.example.quotableapp.view.common.TagsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OneQuoteFragment : Fragment() {

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
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvTags.adapter = tagsAdapter
        viewModel.quote.observe(viewLifecycleOwner) {
            binding.letterIcon.letter = if (it.author.isNotEmpty()) it.author[0].toString() else "?"
            tagsAdapter.submitList(it.tags)
        }
    }
}