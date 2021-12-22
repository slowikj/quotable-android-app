package com.example.quotableapp.ui.allquotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.FragmentAllQuotesBinding
import com.example.quotableapp.databinding.RefreshableRecyclerviewBinding
import com.example.quotableapp.ui.common.quoteslist.QuotesListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlin.time.ExperimentalTime

@ExperimentalPagingApi
@ExperimentalTime
@FlowPreview
@AndroidEntryPoint
class AllQuotesFragment : QuotesListFragment<AllQuotesListViewModel>() {

    private lateinit var binding: FragmentAllQuotesBinding

    override val recyclerViewLayoutBinding: RefreshableRecyclerviewBinding
        get() = binding.recyclerviewLayout

    override val listViewModel: AllQuotesListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllQuotesBinding.inflate(inflater)
        return binding.root
    }

    override fun showQuote(quote: Quote) {
        val action = AllQuotesFragmentDirections.showOneQuote(quote.id)
        findNavController().navigate(action)
    }

    override fun showAuthorFragment(authorSlug: String) {
        val action = AllQuotesFragmentDirections.showAuthor(authorSlug)
        findNavController().navigate(action)
    }

    override fun showQuotesOfTag(tag: String) {
        val action = AllQuotesFragmentDirections.showTagQuotes(tag)
        findNavController().navigate(action)
    }
}