package com.example.quotableapp.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.ItemGridQuoteBinding
import com.example.quotableapp.ui.common.rvAdapters.QuoteDifferentiator

class QuotesAdapter(private val onClick: (Quote) -> Unit) :
    ListAdapter<Quote, QuotesAdapter.ViewHolder>(QuoteDifferentiator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemGridQuoteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick = onClick)
    }

    class ViewHolder(private val binding: ItemGridQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(quote: Quote, onClick: (Quote) -> Unit) {
            binding.model = quote
            binding.root.setOnClickListener { onClick(quote) }
        }
    }
}