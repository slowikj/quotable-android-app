package com.example.quotableapp.ui.tagslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.databinding.ItemListTagBinding
import com.example.quotableapp.ui.common.rvAdapters.TagDifferentiator

class TagsListAdapter(private val onItemClick: (Tag) -> Unit) :
    ListAdapter<Tag, TagsListAdapter.ViewHolder>(TagDifferentiator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            binding = ItemListTagBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onItemClick = onItemClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemListTagBinding,
        private val onItemClick: (Tag) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag) {
            binding.model = tag
            binding.root.setOnClickListener { onItemClick(tag) }
        }
    }

}