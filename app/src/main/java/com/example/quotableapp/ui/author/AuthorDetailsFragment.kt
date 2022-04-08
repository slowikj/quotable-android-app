package com.example.quotableapp.ui.author

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.databinding.FragmentAuthorDetailsBinding
import com.example.quotableapp.ui.common.extensions.handle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@AndroidEntryPoint
class AuthorDetailsFragment : Fragment() {

    private val viewModel: AuthorViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var binding: FragmentAuthorDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorDetailsBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@AuthorDetailsFragment.viewLifecycleOwner
            viewModel = this@AuthorDetailsFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dataLoadHandler.btnRetry.setOnClickListener {
            viewModel.updateAuthor()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authorState.collectLatest {
                    binding.dataLoadHandler.handle(it)
                }
            }
        }
    }
}