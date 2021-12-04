package com.example.quotableapp.view.onequote

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.quotableapp.databinding.OneQuoteFragmentBinding
import com.example.quotableapp.view.allquotes.AllQuotesFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OneQuoteFragment : Fragment() {

    private val viewModel: OneQuoteViewModel by viewModels()

    private lateinit var binding: OneQuoteFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OneQuoteFragmentBinding.inflate(inflater).apply {
            viewModel = this@OneQuoteFragment.viewModel
            lifecycleOwner = this@OneQuoteFragment.viewLifecycleOwner
        }
        return binding.root
    }

}