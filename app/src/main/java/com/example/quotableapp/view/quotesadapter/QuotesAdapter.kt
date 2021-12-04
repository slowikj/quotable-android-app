package com.example.quotableapp.view.quotesadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.ItemQuoteBinding

class QuotesAdapter(private val onItemClick: (Quote) -> Unit) :
    PagingDataAdapter<Quote, QuotesAdapter.ViewHolder>(itemDifferentiator) {

    companion object {
        private val itemDifferentiator = object : DiffUtil.ItemCallback<Quote>() {
            override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            binding = ItemQuoteBinding.inflate(layoutInflater, parent, false),
            onItemClick = onItemClick
        )
    }

    class ViewHolder(
        private val binding: ItemQuoteBinding,
        private val onItemClick: (Quote) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(quote: Quote?) {
            binding.model = quote
            binding.root.setOnClickListener { quote?.let { onItemClick(it) } }
        }
    }
}