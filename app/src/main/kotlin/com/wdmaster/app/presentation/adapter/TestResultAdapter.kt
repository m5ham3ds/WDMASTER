package com.wdmaster.app.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.wdmaster.app.R
import com.wdmaster.app.data.local.entity.TestResultEntity
import com.wdmaster.app.databinding.ItemTestResultBinding
import com.wdmaster.app.presentation.common.BaseAdapter
import java.text.SimpleDateFormat
import java.util.*

class TestResultAdapter : BaseAdapter<TestResultEntity, ItemTestResultBinding>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TestResultEntity>() {
            override fun areItemsTheSame(oldItem: TestResultEntity, newItem: TestResultEntity) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: TestResultEntity, newItem: TestResultEntity) =
                oldItem == newItem
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup) =
        ItemTestResultBinding.inflate(inflater, parent, false)

    override fun bind(binding: ItemTestResultBinding, item: TestResultEntity, position: Int) {
        binding.tvCardCode.text = item.cardCode
        binding.tvTimestamp.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(Date(item.testedAt))
        binding.tvState.text = item.state

        val iconRes = when (item.state) {
            "Success" -> R.drawable.ic_success
            "Failure" -> R.drawable.ic_failure
            else -> R.drawable.ic_info
        }
        binding.ivStatus.setImageResource(iconRes)
    }
}