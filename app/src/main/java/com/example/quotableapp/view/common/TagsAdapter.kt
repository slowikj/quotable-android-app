package com.example.quotableapp.view.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quotableapp.databinding.ItemTagBinding

class TagsAdapter : ListAdapter<String, TagsAdapter.ViewHolder>(itemDifferentiator) {

    companion object {
        val itemDifferentiator = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(binding = ItemTagBinding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemTagBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: String) {
            binding.tagName = tag
        }

    }
}
