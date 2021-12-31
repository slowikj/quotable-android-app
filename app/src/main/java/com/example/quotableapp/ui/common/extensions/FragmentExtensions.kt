package com.example.quotableapp.ui.common.extensions

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quotableapp.R

fun Fragment.showErrorToast(message: String = getString(R.string.error_occurred)) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}