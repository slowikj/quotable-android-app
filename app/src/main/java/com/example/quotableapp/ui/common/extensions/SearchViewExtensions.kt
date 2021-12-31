package com.example.quotableapp.ui.common.extensions

import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun SearchView.getQueryTextChangedStateFlow(): StateFlow<String> {
    val res = MutableStateFlow("")
    this.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            res.value = newText.orEmpty()
            return true
        }
    })
    return res
}