package com.example.quotableapp.ui.tagslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.databinding.FragmentTagsListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TagsListFragment : Fragment() {

    private lateinit var binding: FragmentTagsListBinding

    private val viewModel: TagsListViewModel by viewModels()

    private val tagsAdapter = TagsListAdapter(onItemClick = { showQuotesOfTag(it) })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTagsListBinding.inflate(inflater, container, false).apply {
            rvTags.adapter = tagsAdapter
            dataLoadHandler.btnRetry.setOnClickListener { viewModel.fetchTags() }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                handleTagsList()
            }
        }
    }

    private fun showQuotesOfTag(tag: Tag) {
        val action = TagsListFragmentDirections.showQuotesOfTag(tag.name)
        findNavController().navigate(action)
    }

    private suspend fun handleTagsList() {
        viewModel.tags.collectLatest { state ->
            tagsAdapter.submitList(state.data)
            with(binding) {
                dataLoadHandler.btnRetry.isVisible = state.error != null
                dataLoadHandler.tvError.isVisible = state.error != null
                rvTags.isVisible = state.data?.isNotEmpty() ?: false
                dataLoadHandler.progressBar.isVisible = state.isLoading
            }
        }
    }
}