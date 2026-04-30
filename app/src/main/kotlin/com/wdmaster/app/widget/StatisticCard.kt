package com.wdmaster.app.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.wdmaster.app.R

class StatisticCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val iconView: ImageView
    private val valueView: TextView
    private val titleView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_statistic_card_inner, this, true)
        iconView = findViewById(R.id.iv_stat_icon)
        valueView = findViewById(R.id.tv_stat_value)
        titleView = findViewById(R.id.tv_stat_title)
    }

    fun setValue(value: String) {
        valueView.text = value
    }

    fun setTitle(title: String) {
        titleView.text = title
    }

    fun setColor(color: Int) {
        valueView.setTextColor(color)
        iconView.setColorFilter(color)
    }
}