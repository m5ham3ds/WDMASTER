package com.wdmaster.app.presentation.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R

class AboutDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_about, null)
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(view)
            .create()

        // إغلاق الحوار عند الضغط على "موافق"
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_about_ok)
            ?.setOnClickListener { dismiss() }

        return dialog
    }

    companion object {
        const val TAG = "AboutDialog"
    }
}