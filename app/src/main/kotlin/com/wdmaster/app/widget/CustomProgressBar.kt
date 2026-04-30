package com.wdmaster.app.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.core.view.ViewCompat

class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {

    fun setProgressWithAnimation(progress: Int) {
        if (ViewCompat.isAttachedToWindow(this)) {
            this.progress = progress
        }
    }
}
