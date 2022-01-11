package com.example.quotableapp.ui.authorlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.databinding.ItemGridAuthorBinding

class AuthorsListAdapter(private val onItemClick: (Author) -> Unit) :
    PagingDataAdapter<Author, AuthorsListAdapter.ViewHolder>(differentiator) {

    companion object {
        val differentiator = object : DiffUtil.ItemCallback<Author>() {
            override fun areItemsTheSame(oldItem: Author, newItem: Author): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Author, newItem: Author): Boolean =
                oldItem == newItem

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            binding = ItemGridAuthorBinding.inflate(layoutInflater),
            onItemClick = onItemClick
        )
    }

    class ViewHolder(
        private val binding: ItemGridAuthorBinding,
        private val onItemClick: (Author) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(author: Author?) {
            binding.model = author
            author?.let { binding.root.setOnClickListener { onItemClick(author) } }
        }
    }
}