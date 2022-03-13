package com.example.quotableapp.ui.common

import com.example.quotableapp.data.model.Quote

interface OnQuoteClickListener {

    fun onItemClick(quote: Quote)

    fun onLikeClick(quote: Quote)

    fun onShareClick(quote: Quote)

    fun onCopyClick(quote: Quote)

    fun onAuthorClick(authorSlug: String)

    fun onTagClick(tag: String)
}
