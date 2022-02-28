package com.example.quotableapp.ui.author

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.R
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.FragmentAuthorDetailsBinding
import com.example.quotableapp.databinding.FragmentAuthorQuotesBinding
import com.example.quotableapp.databinding.RefreshableRecyclerviewBinding
import com.example.quotableapp.ui.common.quoteslist.QuotesListFragment
import com.example.quotableapp.ui.common.quoteslist.QuotesProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlin.time.ExperimentalTime

@FlowPreview
@ExperimentalPagingApi
@ExperimentalTime
@AndroidEntryPoint
class AuthorQuotesFragment : QuotesListFragment() {

    private val viewModel: AuthorViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override val quotesProvider: QuotesProvider
        get() = viewModel

    private lateinit var binding: FragmentAuthorQuotesBinding

    override val recyclerViewLayoutBinding: RefreshableRecyclerviewBinding
        get() = binding.recyclerviewLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun showQuote(quote: Quote) {
        val action = AuthorQuotesFragmentDirections.showOneQuote(quote.id)
        findNavController().navigate(action)
    }

    override fun showAuthorFragment(authorSlug: String) {
    }

    override fun showQuotesOfTag(tag: String) {
        val action = AuthorQuotesFragmentDirections.showQuotesOfTag(tag)
        findNavController().navigate(action)
    }
}
