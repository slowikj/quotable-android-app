package com.example.quotableapp.ui.common.formatters

import com.example.quotableapp.data.model.Quote

fun Quote.formatToClipboard(): String = "$content $author"