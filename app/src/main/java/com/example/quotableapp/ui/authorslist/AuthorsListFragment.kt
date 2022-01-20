package com.example.quotableapp.ui.authorslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.GridLayoutManager
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.databinding.FragmentAuthorsListBinding
import com.example.quotableapp.ui.common.extensions.RecyclerViewComposite
import com.example.quotableapp.ui.common.extensions.setupWith
import com.example.quotableapp.ui.common.extensions.showErrorToast
import com.example.quotableapp.ui.common.rvAdapters.DefaultLoadingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalPagingApi
@AndroidEntryPoint
class AuthorsListFragment : Fragment() {

    private val listViewModel: AuthorsListViewModel by viewModels()

    private lateinit var binding: FragmentAuthorsListBinding

    private val authorsListAdapter = AuthorsListAdapter(
        onItemClick = { listViewModel.onAuthorClick(it) }
    )

    private val recyclerViewComposite by lazy {
        RecyclerViewComposite(
            recyclerView = binding.recyclerviewLayout.rvQuotes,
            emptyListLayout = binding.recyclerviewLayout.emptyListLayout.root,
            errorLayout = binding.recyclerviewLayout.dataLoadHandler.errorHandler,
            swipeRefreshLayout = binding.recyclerviewLayout.swipeToRefresh,
            loadingLayout = binding.recyclerviewLayout.dataLoadHandler.progressBar
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorsListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListAdapter()
        setUpAuthorListRecyclerView()
        setupViewModelEventsHandling()
        binding.recyclerviewLayout.dataLoadHandler.btnRetry.setOnClickListener {
            listViewModel.onRefresh()
        }
    }

    private fun setupViewModelEventsHandling() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { collectNavigationActions() }
                launch { collectAuthorsFlow() }
                launch { collectPlainActions() }
            }
        }
    }

    private fun setupListAdapter() {
        recyclerViewComposite.swipeRefreshLayout
            ?.setOnRefreshListener { listViewModel.onRefresh() }
        authorsListAdapter.setupWith(
            recyclerViewComposite = recyclerViewComposite,
            lifecycleCoroutineScope = viewLifecycleOwner.lifecycleScope,
            onError = { showErrorToast() }
        )
    }

    private fun setUpAuthorListRecyclerView() {
        binding.recyclerviewLayout.rvQuotes.apply {
            layoutManager = GridLayoutManager(
                binding.recyclerviewLayout.rvQuotes.context,
                2
            )
            adapter = authorsListAdapter.withLoadStateFooter(
                DefaultLoadingAdapter { authorsListAdapter.retry() }
            )
        }
    }

    private suspend fun collectPlainActions() {
        listViewModel.action.collect {
            when (it) {
                is AuthorsListViewModel.Action.RefreshList -> authorsListAdapter.refresh()
            }
        }
    }

    private suspend fun collectAuthorsFlow() {
        listViewModel.fetchAuthors().collectLatest {
            authorsListAdapter.submitData(it)
        }
    }

    private suspend fun collectNavigationActions() {
        listViewModel.navigationAction.collect {
            when (it) {
                is AuthorsListViewModel.NavigationAction.ToAuthor -> showAuthorFragment(it.author)
            }
        }
    }

    private fun showAuthorFragment(author: Author) {
        val action = AuthorsListFragmentDirections.showAuthor(author.slug)
        findNavController().navigate(action)
    }

}