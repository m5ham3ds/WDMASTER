package com.wdmaster.app.presentation.history

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R
import com.wdmaster.app.databinding.DialogFilterBinding

class FilterDialog : DialogFragment() {

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!
    private var onApply: ((String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFilterBinding.inflate(layoutInflater)
        return MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var selectedStatus = "all"
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                selectedStatus = when (checkedIds[0]) {
                    R.id.chip_success -> "success"
                    R.id.chip_failure -> "failure"
                    else -> "all"
                }
            }
        }
        binding.btnFilterApply.setOnClickListener {
            onApply?.invoke(selectedStatus)
            dismiss()
        }
        binding.btnFilterCancel.setOnClickListener { dismiss() }
    }

    fun setOnFilterApplied(listener: (String) -> Unit) {
        onApply = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
