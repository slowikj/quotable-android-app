package com.example.quotableapp.ui.allquotes

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.R
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.FragmentAllQuotesBinding
import com.example.quotableapp.databinding.RefreshableRecyclerviewBinding
import com.example.quotableapp.ui.common.extensions.changeToolbarColorOnVisibilityChange
import com.example.quotableapp.ui.common.extensions.getColor
import com.example.quotableapp.ui.common.extensions.getQueryTextChangedStateFlow
import com.example.quotableapp.ui.common.quoteslist.QuotesListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@ExperimentalTime
@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class AllQuotesFragment : QuotesListFragment<AllQuotesListViewModel>() {

    private val focusColor by lazy { getColor(R.color.colorAccent) }
    private val notFocusedColor by lazy { getColor(R.color.colorPrimaryDark) }

    private lateinit var binding: FragmentAllQuotesBinding

    override val recyclerViewLayoutBinding: RefreshableRecyclerviewBinding
        get() = binding.recyclerviewLayout

    override val listViewModel: AllQuotesListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllQuotesBinding.inflate(inflater, container, false)
        prepareToolbar()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_all_quotes, menu)
        prepareSearchView(menu)
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
        val action = AllQuotesFragmentDirections.showQuotesOfTag(tag)
        findNavController().navigate(action)
    }

    private fun prepareToolbar() {
        setHasOptionsMenu(true)
        val toolbar = binding.toolbar
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
    }

    private fun prepareSearchView(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = (searchItem.actionView as SearchView).apply {
            queryHint = getString(R.string.search)
            changeToolbarColorOnVisibilityChange(
                focusColor = focusColor,
                notFocusedColor = notFocusedColor,
                toolbar = binding.toolbar
            )
        }

        searchView.setQuery(listViewModel.lastSearchQuery.value, true)
        observeOnSearchQueryChanged(searchView)
    }

    private fun observeOnSearchQueryChanged(searchView: SearchView) {
        viewLifecycleOwner.lifecycleScope.launch {
            searchView.getQueryTextChangedStateFlow()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    listViewModel.onSearchQueryChanged(it)
                }
        }
    }
}