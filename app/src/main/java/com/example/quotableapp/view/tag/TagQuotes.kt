package com.example.quotableapp.view.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.FragmentTagQuotesBinding
import com.example.quotableapp.view.common.quoteslist.QuotesListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlin.time.ExperimentalTime

@ExperimentalPagingApi
@ExperimentalTime
@FlowPreview
@AndroidEntryPoint
class TagQuotes : QuotesListFragment<TagQuotesListViewModel>() {

    private lateinit var binding: FragmentTagQuotesBinding

    override val listViewModel: TagQuotesListViewModel by viewModels()

    override val rvQuotes: RecyclerView
        get() = binding.rvQuotes

    override val swipeToRefresh: SwipeRefreshLayout
        get() = binding.swipeToRefresh

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTagQuotesBinding.inflate(inflater)
        return binding.root
    }

    override fun showAuthorFragment(authorSlug: String) {
        val action = TagQuotesDirections.showAuthor(authorSlug)
        findNavController().navigate(action)
    }

    override fun showQuote(quote: Quote) {
        val action = TagQuotesDirections.showOneQuote(quote.id)
        findNavController().navigate(action)
    }

    override fun showQuotesOfTag(tag: String) {
    }

}