package com.example.quotableapp.view.common.quoteslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.ItemQuoteBinding
import com.example.quotableapp.view.common.rvAdapters.TagsAdapter

class QuotesAdapter(
    private val onClickHandler: ViewHolder.OnClickHandler
) :
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
            onClickHandler = onClickHandler
        )
    }

    class ViewHolder(
        private val binding: ItemQuoteBinding,
        private val onClickHandler: OnClickHandler
    ) : RecyclerView.ViewHolder(binding.root) {

        private val tagsAdapter = TagsAdapter { onClickHandler.onTag(it) }

        init {
            binding.rvTags.adapter = tagsAdapter
        }

        fun bind(quote: Quote?) {
            binding.model = quote
            binding.onClickHandler = onClickHandler
            tagsAdapter.submitList(quote?.tags)
        }

        interface OnClickHandler {

            fun onItem(quote: Quote)

            fun onAuthor(quote: Quote)

            fun onTag(tag: String)
        }
    }
}
