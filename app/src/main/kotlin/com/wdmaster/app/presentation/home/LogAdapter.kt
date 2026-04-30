package com.wdmaster.app.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.wdmaster.app.R
import com.wdmaster.app.domain.model.LogEntry
import com.wdmaster.app.domain.model.LogLevel
import com.wdmaster.app.databinding.ItemCardLogBinding
import com.wdmaster.app.presentation.common.BaseAdapter
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter : BaseAdapter<LogEntry, ItemCardLogBinding>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LogEntry>() {
            override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean =
                oldItem.timestamp == newItem.timestamp && oldItem.message == newItem.message

            override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean =
                oldItem == newItem
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemCardLogBinding {
        return ItemCardLogBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemCardLogBinding, item: LogEntry, position: Int) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        binding.tvLogTime.text = timeFormat.format(Date(item.timestamp))

        binding.tvLogMessage.text = item.message
        when (item.level) {
            LogLevel.SUCCESS -> {
                binding.ivLogIcon.setImageResource(R.drawable.ic_success)
                binding.tvLogMessage.setTextColor(binding.root.context.getColor(R.color.log_text_success))
            }
            LogLevel.ERROR -> {
                binding.ivLogIcon.setImageResource(R.drawable.ic_failure)
                binding.tvLogMessage.setTextColor(binding.root.context.getColor(R.color.log_text_failure))
            }
            LogLevel.WARNING -> {
                binding.ivLogIcon.setImageResource(R.drawable.ic_warning)
                binding.tvLogMessage.setTextColor(binding.root.context.getColor(R.color.log_text_warning))
            }
            else -> {
                binding.ivLogIcon.setImageResource(R.drawable.ic_info)
                binding.tvLogMessage.setTextColor(binding.root.context.getColor(R.color.log_text_default))
            }
        }
    }
}