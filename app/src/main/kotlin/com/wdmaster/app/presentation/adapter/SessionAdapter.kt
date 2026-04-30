package com.wdmaster.app.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.wdmaster.app.R
import com.wdmaster.app.data.local.entity.TestSessionEntity
import com.wdmaster.app.databinding.ItemSessionBinding
import com.wdmaster.app.presentation.common.BaseAdapter
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(
    private val onSessionClick: (TestSessionEntity) -> Unit
) : BaseAdapter<TestSessionEntity, ItemSessionBinding>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TestSessionEntity>() {
            override fun areItemsTheSame(oldItem: TestSessionEntity, newItem: TestSessionEntity) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: TestSessionEntity, newItem: TestSessionEntity) =
                oldItem == newItem
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup) =
        ItemSessionBinding.inflate(inflater, parent, false)

    override fun bind(binding: ItemSessionBinding, item: TestSessionEntity, position: Int) {
        binding.tvSessionSummary.text = "Session #${item.id}"
        binding.tvSessionDate.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(item.startedAt))

        binding.root.setOnClickListener { onSessionClick(item) }
        binding.ivActiveIndicator.visibility = if (item.isRunning) View.VISIBLE else View.GONE
    }
}
