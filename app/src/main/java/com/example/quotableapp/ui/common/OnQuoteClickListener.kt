package com.example.quotableapp.ui.common

import com.example.quotableapp.data.model.Quote

interface OnQuoteClickListener {

    fun onItemClick(quote: Quote)

    fun onItemLongClick(quote: Quote): Boolean

    fun onAuthorClick(authorSlug: String)

    fun onTagClick(tag: String)
}
