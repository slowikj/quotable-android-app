package com.example.quotableapp.ui.author

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.R
import com.example.quotableapp.databinding.FragmentAuthorBinding
import com.example.quotableapp.ui.common.extensions.handle
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@ExperimentalPagingApi
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
            collapsingToolbar.viewModel = viewModel
            collapsingToolbar.lifecycleOwner = this@AuthorFragment.viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleToolbar()
        handleNavigationFlow()
        setupViewPager()
    }

    private fun handleNavigationFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel
                .navigationActions
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    handle(it)
                }
        }
    }

    private fun handle(action: AuthorViewModel.NavigationAction) =
        when (action) {
            is AuthorViewModel.NavigationAction.ToOneQuote -> showOneQuote(action.quoteId)
            is AuthorViewModel.NavigationAction.ToQuotesOfTag -> showQuotesOfTag(action.tag)
        }

    private fun showOneQuote(quoteId: String) {
        val action = AuthorFragmentDirections.showOneQuote(quoteId)
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

    private fun handleToolbar() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel
                .author
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    binding.collapsingToolbar.dataLoadHandler.handle(it)
                }
        }
    }

}