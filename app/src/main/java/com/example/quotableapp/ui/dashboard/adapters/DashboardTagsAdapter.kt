package com.example.quotableapp.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.databinding.DashboardItemTagBinding
import com.example.quotableapp.ui.common.rvAdapters.TagDifferentiator

class DashboardTagsAdapter(private val onClick: (Tag) -> Unit) :
    ListAdapter<Tag, DashboardTagsAdapter.ViewHolder>(TagDifferentiator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            binding = DashboardItemTagBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClick = onClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: DashboardItemTagBinding,
        private val onClick: (Tag) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: Tag) {
            binding.name = tag.name
            binding.card.setOnClickListener { onClick(tag) }
        }
    }
}