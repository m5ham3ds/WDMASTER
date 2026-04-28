package com.wdmaster.app.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.wdmaster.app.R

class ConnectionStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val statusText: TextView
    private val colorBar: View

    init {
        inflate(context, R.layout.widget_connection_status, this)
        statusText = findViewById(R.id.tv_status_text)
        colorBar = findViewById(R.id.view_color_bar)
    }

    fun setStatus(text: String, color: Int) {
        statusText.text = text
        colorBar.setBackgroundColor(color)
    }
}
