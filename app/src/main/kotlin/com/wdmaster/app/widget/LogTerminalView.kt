package com.wdmaster.app.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wdmaster.app.R

class LogTerminalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    val recyclerView: RecyclerView

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_log_terminal, this, true)
        recyclerView = findViewById(R.id.recycler_log)
    }

    fun setup(adapter: RecyclerView.Adapter<*>) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
}