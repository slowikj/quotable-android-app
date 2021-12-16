package com.example.quotableapp.view.authorlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.example.quotableapp.R
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.databinding.FragmentAuthorsListBinding
import com.example.quotableapp.view.common.DefaultLoadingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

@ExperimentalPagingApi
@AndroidEntryPoint
class AuthorsListFragment : Fragment() {

    private val listViewModel: AuthorsListViewModel by viewModels()

    private lateinit var binding: FragmentAuthorsListBinding

    private val authorsListAdapter =
        AuthorsListAdapter { listViewModel.onAuthorClick(it) }

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
        setUpAuthorListRecyclerView()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { collectNavigationActions() }
                launch { collectAuthorsFlow() }
                launch { collectOtherActions() }
            }
        }
        setupPullToRefresh()
    }

    private fun setupPullToRefresh() {
        binding.recyclerviewLayout.swipeToRefresh.setOnRefreshListener { listViewModel.onRefresh() }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                authorsListAdapter
                    .loadStateFlow
                    .distinctUntilChangedBy { it.refresh }
                    .collectLatest { loadStates ->
                        binding.recyclerviewLayout.swipeToRefresh.isRefreshing =
                            loadStates.refresh is LoadState.Loading
                        if (loadStates.refresh is LoadState.Error) {
                            showErrorToast()
                        }
                    }
            }
        }
    }

    private fun setUpAuthorListRecyclerView() {
        binding.recyclerviewLayout.rvQuotes.apply {
            layoutManager = GridLayoutManager(binding.recyclerviewLayout.rvQuotes.context, 2)
            adapter =
                authorsListAdapter.withLoadStateFooter(DefaultLoadingAdapter { authorsListAdapter.retry() })
        }
    }

    private suspend fun collectOtherActions() {
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

    private fun showErrorToast() {
        Toast.makeText(
            context,
            getString(R.string.error_occurred),
            Toast.LENGTH_SHORT
        ).show()
    }
}