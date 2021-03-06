package com.example.quotableapp.ui.author

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.R
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.FragmentAuthorBinding
import com.example.quotableapp.ui.common.extensions.handle
import com.example.quotableapp.ui.common.extensions.showErrorToast
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@ExperimentalTime
@FlowPreview
@AndroidEntryPoint
class AuthorFragment : Fragment() {

    private val viewModel: AuthorViewModel by viewModels(ownerProducer = { this })

    private lateinit var binding: FragmentAuthorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@AuthorFragment.viewLifecycleOwner
            viewModel = this@AuthorFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleAuthorStateUpdates()
        handleNavigationFlow()
        setupViewPager()
        binding.dataLoadHandlerToolbar.btnRetry.setOnClickListener {
            viewModel.updateAuthor()
        }
    }

    private fun handleNavigationFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigationActions
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    handle(it)
                }
        }
    }

    private fun handle(action: AuthorViewModel.NavigationAction) =
        when (action) {
            is AuthorViewModel.NavigationAction.ToOneQuote -> showOneQuote(action.quote)
            is AuthorViewModel.NavigationAction.ToQuotesOfTag -> showQuotesOfTag(action.tag)
        }

    private fun showOneQuote(quote: Quote) {
        val action = AuthorFragmentDirections.showOneQuote(quote)
        findNavController().navigate(action)
    }

    private fun showQuotesOfTag(tag: String) {
        val action = AuthorFragmentDirections.showQuotesOfTag(tag)
        findNavController().navigate(action)
    }

    private fun setupViewPager() {
        val adapter = AuthorViewPagerAdapter(binding.viewPager.findFragment())
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.viewpager_author_tab_bio)
                1 -> getString(R.string.viewpager_author_tab_quotes)
                else -> ""
            }
        }.attach()
    }

    private fun handleAuthorStateUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel
                .authorState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    binding.dataLoadHandlerToolbar.handle(it)
                    showErrorToastIfNoData(it)
                }
        }
    }

    private fun showErrorToastIfNoData(authorUiState: AuthorUiState) {
        if (authorUiState.data != null && authorUiState.error != null) {
            showErrorToast()
            viewModel.consumeError(authorUiState.error)
        }
    }

}