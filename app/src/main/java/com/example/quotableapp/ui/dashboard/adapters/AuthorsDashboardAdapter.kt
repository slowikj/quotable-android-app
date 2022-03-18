package com.example.quotableapp.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.databinding.DashboardItemAuthorBinding
import com.example.quotableapp.ui.common.rvAdapters.AuthorDifferentiator

class AuthorsDashboardAdapter(private val onClick: (Author) -> Unit) :
    ListAdapter<Author, AuthorsDashboardAdapter.ViewHolder>(AuthorDifferentiator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DashboardItemAuthorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick = onClick)
    }

    class ViewHolder(private val binding: DashboardItemAuthorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(author: Author, onClick: (Author) -> Unit) {
            binding.model = author
            binding.root.setOnClickListener { onClick(author) }
        }
    }
}