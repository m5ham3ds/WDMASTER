package com.wdmaster.app.presentation.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R

class GenerationConfigDialog : DialogFragment() {

    private var onApply: ((seed: Long, limit: Int) -> Unit)? = null

    fun setOnApplyListener(listener: (seed: Long, limit: Int) -> Unit) {
        onApply = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_generation_config, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()

        view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_seed)
        view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_limit)

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)?.setOnClickListener {
            dismiss()
        }
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_apply)?.setOnClickListener {
            val seed = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_seed)
                .text.toString().toLongOrNull() ?: 0L
            val limit = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_limit)
                .text.toString().toIntOrNull() ?: 100
            onApply?.invoke(seed, limit)
            dismiss()
        }

        return dialog
    }

    companion object {
        const val TAG = "GenerationConfigDialog"
    }
}
