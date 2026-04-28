package com.wdmaster.app.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.wdmaster.app.data.local.entity.SuccessfulPatternEntity
import com.wdmaster.app.databinding.ItemPatternBinding
import com.wdmaster.app.presentation.common.BaseAdapter
import java.text.SimpleDateFormat
import java.util.*

class PatternAdapter : BaseAdapter<SuccessfulPatternEntity, ItemPatternBinding>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SuccessfulPatternEntity>() {
            override fun areItemsTheSame(oldItem: SuccessfulPatternEntity, newItem: SuccessfulPatternEntity) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: SuccessfulPatternEntity, newItem: SuccessfulPatternEntity) =
                oldItem == newItem
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup) =
        ItemPatternBinding.inflate(inflater, parent, false)

    override fun bind(binding: ItemPatternBinding, item: SuccessfulPatternEntity, position: Int) {
        binding.tvPatternCode.text = item.code
        binding.tvPatternDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(item.discoveredAt))
    }
}