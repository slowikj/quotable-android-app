package com.example.quotableapp.ui.common.quoteslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.databinding.ItemListQuoteBinding
import com.example.quotableapp.ui.common.rvAdapters.QuoteDifferentiator
import com.example.quotableapp.ui.common.rvAdapters.TagsAdapter

class QuotesAdapter(
    private val onClickHandler: ViewHolder.OnClickHandler
) : PagingDataAdapter<Quote, QuotesAdapter.ViewHolder>(QuoteDifferentiator()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            binding = ItemListQuoteBinding.inflate(layoutInflater, parent, false),
            onClickHandler = onClickHandler
        )
    }

    class ViewHolder(
        private val binding: ItemListQuoteBinding,
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
