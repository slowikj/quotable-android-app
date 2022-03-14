package com.example.quotableapp.ui.common.rvAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.databinding.QuoteItemListTagBinding

class QuoteTagsAdapter(private val onClick: (String) -> Unit) :
    ListAdapter<String, QuoteTagsAdapter.ViewHolder>(StringDifferentiator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            binding = QuoteItemListTagBinding.inflate(layoutInflater),
            onClick = onClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: QuoteItemListTagBinding,
        private val onClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: String) {
            binding.tagName = tag
            binding.root.setOnClickListener { onClick(tag) }
        }
    }
}
