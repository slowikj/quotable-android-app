package com.example.quotableapp.view.common.rvAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.databinding.ItemLoadingBinding

class DefaultLoadingAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<DefaultLoadingAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemLoadingBinding.inflate(layoutInflater, parent, false),
            retry
        )
    }

    class ViewHolder(
        private val binding: ItemLoadingBinding,
        private val retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            with(binding) {
                if (loadState is LoadState.Error) {
                    errorMessage.text = loadState.error.localizedMessage
                }
                btnRetry.isVisible = loadState is LoadState.Error
                errorMessage.isVisible = loadState is LoadState.Error
                progressBar.isVisible = loadState is LoadState.Loading
                btnRetry.setOnClickListener { retry() }
            }
        }
    }
}