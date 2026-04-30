package com.wdmaster.app.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.Toolbar

class CustomToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Toolbar(context, attrs, defStyleAttr) {

    fun setDynamicTitle(title: String) {
        this.title = title
    }

    fun setTitleColor(color: Int) {
        setTitleTextColor(color)
    }
}