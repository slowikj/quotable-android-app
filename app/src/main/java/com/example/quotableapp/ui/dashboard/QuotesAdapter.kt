package com.example.quotableapp.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.ItemGridQuoteBinding

class QuotesAdapter(private val onClick: (Quote) -> Unit) : ListAdapter<Quote, QuotesAdapter.ViewHolder>(differentiator) {

    companion object {
        val differentiator = object : DiffUtil.ItemCallback<Quote>() {
            override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGridQuoteBinding.inflate(LayoutInflater.from(parent.context)))
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