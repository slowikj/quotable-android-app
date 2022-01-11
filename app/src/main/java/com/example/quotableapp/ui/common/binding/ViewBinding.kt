package com.example.quotableapp.ui.common.binding

import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object ViewBinding {

    @JvmStatic
    @BindingAdapter("loadImage")
    fun bindLoadImage(view: AppCompatImageView, url: String?) {
        Glide.with(view.context)
            .load(url)
            .into(view)
    }
    
    @JvmStatic
    @BindingAdapter("bindFirstMeaningfulTag")
    fun bindFirstMeaningfulTag(view: TextView, tags: List<String>) {
        val generalTag = "famous-quotes"
        view.text = tags.firstOrNull { it != generalTag } ?: generalTag
    }
}