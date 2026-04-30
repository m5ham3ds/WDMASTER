package com.wdmaster.app.presentation.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R

class RouterTestDialog : DialogFragment() {

    private var onConfirm: (() -> Unit)? = null

    companion object {
        fun newInstance(routerName: String): RouterTestDialog {
            val dialog = RouterTestDialog()
            val args = Bundle()
            args.putString("routerName", routerName)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val routerName = arguments?.getString("routerName") ?: ""
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm, null)
        val titleView = view.findViewById<android.widget.TextView>(R.id.tv_confirm_title)
        val messageView = view.findViewById<android.widget.TextView>(R.id.tv_confirm_message)
        titleView?.text = "Test $routerName"
        messageView?.text = "Start a test on this router?"

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(view)
            .create()

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_confirm_ok)?.setOnClickListener {
            onConfirm?.invoke()
            dismiss()
        }
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_confirm_cancel)?.setOnClickListener { dismiss() }

        return dialog
    }

    fun setOnConfirmListener(listener: () -> Unit) {
        onConfirm = listener
    }
}