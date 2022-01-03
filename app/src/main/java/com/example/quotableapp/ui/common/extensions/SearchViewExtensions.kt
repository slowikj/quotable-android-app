package com.example.quotableapp.ui.common.extensions

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun SearchView.getQueryTextChangedStateFlow(): StateFlow<String> {
    val res = MutableStateFlow("")
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

fun SearchView.changeToolbarColorOnVisibilityChange(focusColor: Int, notFocusedColor: Int, toolbar: Toolbar) {
    val searchView = this
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
            setToolbarColor(searchView, toolbar, focusColor)
        }

        override fun onViewDetachedFromWindow(v: View?) {
            setToolbarColor(searchView, toolbar, notFocusedColor)
        }
    })
}

private fun setToolbarColor(searchView: SearchView, toolbar: Toolbar, color: Int) {
    searchView.setBackgroundColor(color)
    toolbar.setBackgroundColor(color)
}