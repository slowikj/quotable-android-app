package com.example.quotableapp.ui.dashboard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.databinding.ItemGridAuthorBinding

class AuthorsAdapter(private val onClick: (Author) -> Unit) : ListAdapter<Author, AuthorsAdapter.ViewHolder>(
    itemDifferentiator
) {

    companion object {
        val itemDifferentiator = object : DiffUtil.ItemCallback<Author>() {
            override fun areItemsTheSame(oldItem: Author, newItem: Author): Boolean {
                return oldItem.slug == newItem.slug
            }

            override fun areContentsTheSame(oldItem: Author, newItem: Author): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGridAuthorBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick = onClick)
    }

    class ViewHolder(private val binding: ItemGridAuthorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(author: Author, onClick: (Author) -> Unit) {
            binding.model = author
            binding.root.setOnClickListener { onClick(author)}
        }
    }
}