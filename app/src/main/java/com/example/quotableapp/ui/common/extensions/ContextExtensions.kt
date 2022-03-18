package com.example.quotableapp.ui.common.extensions

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.AttrRes

fun Context.getColorFrom(
    @AttrRes colorAttr: Int
): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(colorAttr, typedValue, true)
    return typedValue.data
}

val Context.isLandscapeMode: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE