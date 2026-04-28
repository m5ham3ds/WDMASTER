package com.wdmaster.app.presentation.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.databinding.DialogAddRouterBinding

class EditRouterDialog : DialogFragment() {

    private var _binding: DialogAddRouterBinding? = null
    private val binding get() = _binding!!
    private var router: RouterProfileEntity? = null
    private var onRouterUpdated: ((RouterProfileEntity) -> Unit)? = null

    companion object {
        fun newInstance(router: RouterProfileEntity): EditRouterDialog {
            val dialog = EditRouterDialog()
            val args = Bundle()
            args.putParcelable("router", router)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        router = arguments?.getParcelable("router")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddRouterBinding.inflate(LayoutInflater.from(requireContext()))
        binding.etRouterName.setText(router?.name ?: "")
        binding.etRouterIp.setText(router?.ip ?: "")
        binding.etRouterPort.setText(router?.port?.toString() ?: "80")
        binding.etRouterUsername.setText(router?.username ?: "")
        binding.etRouterPassword.setText(router?.password ?: "")
        return MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSave.setOnClickListener {
            val updated = router?.copy(
                name = binding.etRouterName.text.toString(),
                ip = binding.etRouterIp.text.toString(),
                port = binding.etRouterPort.text?.toString()?.toIntOrNull() ?: 80,
                username = binding.etRouterUsername.text.toString(),
                password = binding.etRouterPassword.text.toString()
            ) ?: return@setOnClickListener
            onRouterUpdated?.invoke(updated)
            dismiss()
        }
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    fun setOnRouterUpdatedListener(listener: (RouterProfileEntity) -> Unit) {
        onRouterUpdated = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
