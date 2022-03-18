package com.example.quotableapp.ui.authorslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.databinding.ItemListAuthorBinding
import com.example.quotableapp.ui.common.rvAdapters.AuthorDifferentiator

class AuthorsListAdapter(private val onItemClick: (Author) -> Unit) :
    PagingDataAdapter<Author, AuthorsListAdapter.ViewHolder>(AuthorDifferentiator()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            binding = ItemListAuthorBinding.inflate(layoutInflater, parent, false),
            onItemClick = onItemClick
        )
    }

    class ViewHolder(
        private val binding: ItemListAuthorBinding,
        private val onItemClick: (Author) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(author: Author?) {
            binding.model = author
            author?.let { binding.root.setOnClickListener { onItemClick(author) } }
        }
    }
}