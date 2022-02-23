package com.example.quotableapp.ui.tagquotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.FragmentTagQuotesBinding
import com.example.quotableapp.databinding.RefreshableRecyclerviewBinding
import com.example.quotableapp.ui.common.quoteslist.QuotesListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlin.time.ExperimentalTime

@ExperimentalPagingApi
@ExperimentalTime
@FlowPreview
@AndroidEntryPoint
class TagQuotesFragment : QuotesListFragment<TagQuotesListViewModel>() {

    private lateinit var binding: FragmentTagQuotesBinding

    override val recyclerViewLayoutBinding: RefreshableRecyclerviewBinding
        get() = binding.recyclerviewLayout

    override val listViewModel: TagQuotesListViewModel by viewModels()

    private val tagDetailsViewModel: TagDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTagQuotesBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@TagQuotesFragment.viewLifecycleOwner
            tagName = tagDetailsViewModel.tagName
        }
        return binding.root
    }

    override fun showAuthorFragment(authorSlug: String) {
        val action = TagQuotesFragmentDirections.showAuthor(authorSlug)
        findNavController().navigate(action)
    }

    override fun showQuote(quote: Quote) {
        val action = TagQuotesFragmentDirections.showOneQuote(quote.id)
        findNavController().navigate(action)
    }

    override fun showQuotesOfTag(tag: String) {
        if (tagDetailsViewModel.tagName != tag) {
            val action = TagQuotesFragmentDirections.showQuotesOfTag(tag)
            findNavController().navigate(action)
        }
    }

}