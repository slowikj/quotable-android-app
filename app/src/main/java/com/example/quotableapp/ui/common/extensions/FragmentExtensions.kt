package com.example.quotableapp.ui.common.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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

fun Fragment.copyQuoteToClipBoardWithToast(text: String) {
    copyToClipBoardWithToast(label = "quote", text = text)
}

fun Fragment.copyToClipBoardWithToast(label: String, text: String) {
    copyToClipBoard(label = label, text = text)
    showToast(getString(R.string.clipboard_copied_message))
}

fun Fragment.copyToClipBoard(label: String, text: String) {
    val clipboardManager =
        activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clipData)
}