package com.wdmaster.app.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.wdmaster.app.R
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.databinding.ItemRouterProfileBinding
import com.wdmaster.app.presentation.common.BaseAdapter
import java.text.SimpleDateFormat
import java.util.*

class RouterProfileAdapter(
    private val onTestClick: (RouterProfileEntity) -> Unit,
    private val onEditClick: (RouterProfileEntity) -> Unit,
    private val onDeleteClick: (RouterProfileEntity) -> Unit
) : BaseAdapter<RouterProfileEntity, ItemRouterProfileBinding>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RouterProfileEntity>() {
            override fun areItemsTheSame(oldItem: RouterProfileEntity, newItem: RouterProfileEntity) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: RouterProfileEntity, newItem: RouterProfileEntity) =
                oldItem == newItem
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup) =
        ItemRouterProfileBinding.inflate(inflater, parent, false)

    override fun bind(binding: ItemRouterProfileBinding, item: RouterProfileEntity, position: Int) {
        binding.tvRouterName.text = item.name
        binding.tvRouterIp.text = item.ip
        binding.tvRouterType.text = item.protocol
        binding.tvRouterDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(item.createdAt))

        binding.chipDefault.visibility = if (item.isDefault) View.VISIBLE else View.GONE

        binding.btnTest.setOnClickListener { onTestClick(item) }
        binding.btnEdit.setOnClickListener { onEditClick(item) }
        binding.btnDelete.setOnClickListener { onDeleteClick(item) }
    }
}
