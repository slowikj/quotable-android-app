package com.example.quotableapp.ui.author

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.FragmentAuthorBinding
import com.example.quotableapp.databinding.RefreshableRecyclerviewBinding
import com.example.quotableapp.ui.common.extensions.handle
import com.example.quotableapp.ui.common.quoteslist.QuotesListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.ExperimentalTime

@ExperimentalPagingApi
@ExperimentalTime
@FlowPreview
@AndroidEntryPoint
class AuthorFragment : QuotesListFragment<AuthorQuotesViewModel>() {

    override val listViewModel: AuthorQuotesViewModel by viewModels()

    private val authorDetailsViewModel: AuthorDetailsViewModel by viewModels()

    private lateinit var binding: FragmentAuthorBinding

    override val recyclerViewLayoutBinding: RefreshableRecyclerviewBinding
        get() = binding.recyclerviewLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorBinding.inflate(inflater, container, false).apply {
            collapsingToolbar.authorDetailsViewModel = authorDetailsViewModel
            collapsingToolbar.lifecycleOwner = this@AuthorFragment.viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            authorDetailsViewModel.state.collectLatest {
                binding.collapsingToolbar.dataLoadHandler.handle(it)
            }
        }

        binding.collapsingToolbar.dataLoadHandler.btnRetry.setOnClickListener {
            authorDetailsViewModel.onRefresh()
        }
    }

    override fun showQuote(quote: Quote) {
        val action = AuthorFragmentDirections.showOneQuote(quote.id)
        findNavController().navigate(action)
    }

    override fun showAuthorFragment(authorSlug: String) {
    }

    override fun showQuotesOfTag(tag: String) {
        val action = AuthorFragmentDirections.showQuotesOfTag(tag)
        findNavController().navigate(action)
    }
}