package com.wdmaster.app.presentation.history

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R
import com.wdmaster.app.databinding.DialogExportBinding

class ExportDialog : DialogFragment() {

    private var _binding: DialogExportBinding? = null
    private val binding get() = _binding!!
    private var onExport: ((String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogExportBinding.inflate(layoutInflater)
        return MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnExportConfirm.setOnClickListener {
            val fileName = binding.etExportFilename.text.toString().ifEmpty { "results_export.json" }
            onExport?.invoke(fileName)
            dismiss()
        }
        binding.btnExportCancel.setOnClickListener { dismiss() }
    }

    fun setOnExportListener(listener: (String) -> Unit) {
        onExport = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
