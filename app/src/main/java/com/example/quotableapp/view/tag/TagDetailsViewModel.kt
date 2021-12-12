package com.example.quotableapp.view.tag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TagDetailsViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) :
    ViewModel() {

    companion object {
        const val TAG_NAME_ID = "tag"
    }

    val tagName: String = savedStateHandle[TAG_NAME_ID]!!
}