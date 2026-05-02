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

class AddRouterDialog : DialogFragment() {

    private var _binding: DialogAddRouterBinding? = null
    private val binding get() = _binding!!
    private var onRouterSaved: ((RouterProfileEntity) -> Unit)? = null

    fun setOnRouterSavedListener(listener: (RouterProfileEntity) -> Unit) {
        onRouterSaved = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddRouterBinding.inflate(LayoutInflater.from(requireContext()))
        return MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSave.setOnClickListener {
            val router = RouterProfileEntity(
                name = binding.etRouterName.text.toString(),
                ip = binding.etRouterIp.text.toString(),
                port = binding.etRouterPort.text?.toString()?.toIntOrNull() ?: 80,
                protocol = binding.etRouterProtocol.text.toString(),
                loginPath = binding.etRouterLoginPath.text.toString(),
                usernameSelector = binding.etUsernameSelector.text.toString(),
                passwordSelector = binding.etPasswordSelector.text.toString(),
                submitSelector = binding.etSubmitSelector.text.toString(),
                successIndicator = binding.etSuccessIndicator.text.toString(),
                failureIndicator = binding.etFailureIndicator.text.toString(),
                md5Salt = binding.etMd5Salt.text.toString(),
                logoutSelector = binding.etLogoutSelector.text.toString()
            )
            onRouterSaved?.invoke(router)
            dismiss()
        }
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
