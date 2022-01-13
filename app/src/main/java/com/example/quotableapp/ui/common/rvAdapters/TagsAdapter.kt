package com.example.quotableapp.ui.common.rvAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.databinding.ItemTagBinding

class TagsAdapter(private val onClick: (String) -> Unit) :
    ListAdapter<String, TagsAdapter.ViewHolder>(StringDifferentiator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            binding = ItemTagBinding.inflate(layoutInflater),
            onClick = onClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemTagBinding,
        private val onClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: String) {
            binding.tagName = tag
            binding.root.setOnClickListener { onClick(tag) }
        }
    }
}
