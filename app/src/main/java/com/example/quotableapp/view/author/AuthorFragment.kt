package com.example.quotableapp.view.author

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
import com.example.quotableapp.databinding.FragmentAuthorBinding
import com.example.quotableapp.view.common.quoteslist.QuotesListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlin.time.ExperimentalTime

@ExperimentalPagingApi
@ExperimentalTime
@FlowPreview
@AndroidEntryPoint
class AuthorFragment : QuotesListFragment<AuthorViewModel>() {

    override val listViewModel: AuthorViewModel by viewModels()

    private lateinit var binding: FragmentAuthorBinding

    override val rvQuotes: RecyclerView
        get() = binding.rvQuotes

    override val swipeToRefresh: SwipeRefreshLayout
        get() = binding.swipeToRefresh

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorBinding.inflate(inflater).apply {
            lifecycleOwner = this@AuthorFragment.viewLifecycleOwner
            authorName = listViewModel.keyword
        }
        return binding.root
    }

    override fun showQuote(quote: Quote) {
        val action = AuthorFragmentDirections.showOneQuote(quote.id)
        findNavController().navigate(action)
    }

    override fun showAuthorFragment(authorSlug: String) {
    }

    override fun showQuotesOfTag(tag: String) {
        val action = AuthorFragmentDirections.showTagQuotes(tag)
        findNavController().navigate(action)
    }
}