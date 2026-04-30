package com.wdmaster.app.presentation.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R

class ExitDialog : DialogFragment() {

    private var onExitConfirmed: (() -> Unit)? = null

    fun setOnExitConfirmedListener(listener: () -> Unit) {
        onExitConfirmed = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_exit, null)

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(view)
            .create()

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_exit_confirm)?.setOnClickListener {
            onExitConfirmed?.invoke()
            dismiss()
        }
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_exit_cancel)?.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    companion object {
        const val TAG = "ExitDialog"
    }
}