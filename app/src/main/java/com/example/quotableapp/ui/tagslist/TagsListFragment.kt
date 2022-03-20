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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.databinding.FragmentTagsListBinding
import com.example.quotableapp.ui.common.extensions.handle
import com.example.quotableapp.ui.common.extensions.isLandscapeMode
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TagsListFragment : Fragment() {

    companion object {
        private const val ITEMS_SPAN_LANDSCAPE = 4
        private const val ITEMS_SPAN_PORTRAIT  = 2
    }

    private lateinit var binding: FragmentTagsListBinding

    private val viewModel: TagsListViewModel by viewModels()

    private val tagsAdapter = TagsListAdapter(onItemClick = { showQuotesOfTag(it) })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTagsListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@TagsListFragment
            rvTags.adapter = tagsAdapter
            rvTags.itemAnimator = SlideInUpAnimator()
            rvTags.layoutManager = GridLayoutManager(
                context,
                if (requireContext().isLandscapeMode) ITEMS_SPAN_LANDSCAPE else ITEMS_SPAN_PORTRAIT
            )
            dataLoadHandler.btnRetry.setOnClickListener { viewModel.updateTags() }
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
                binding.dataLoadHandler.handle(state)
                rvTags.isVisible = state.data?.isNotEmpty() ?: false
            }
        }
    }
}