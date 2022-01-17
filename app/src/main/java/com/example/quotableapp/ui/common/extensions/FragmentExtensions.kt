package com.example.quotableapp.ui.common.extensions

import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import com.example.quotableapp.R

fun Fragment.showErrorToast() {
    showToast(getString(R.string.error_occurred))
}

fun Fragment.showToast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.getColor(@ColorRes colorRes: Int): Int = requireActivity().getColor(colorRes)