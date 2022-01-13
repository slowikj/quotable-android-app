package com.example.quotableapp.ui.common.rvAdapters

import androidx.recyclerview.widget.DiffUtil
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.Tag

class StringDifferentiator : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}

class TagDifferentiator : DiffUtil.ItemCallback<Tag>() {
    override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
        return oldItem == newItem
    }
}

class AuthorDifferentiator : DiffUtil.ItemCallback<Author>() {
    override fun areItemsTheSame(oldItem: Author, newItem: Author): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Author, newItem: Author): Boolean =
        oldItem == newItem
}

class QuoteDifferentiator : DiffUtil.ItemCallback<Quote>() {
    override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
        return oldItem == newItem
    }
}
