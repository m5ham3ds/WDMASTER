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
        // تعبئة البيانات الحالية
        router?.let {
            binding.etRouterName.setText(it.name)
            binding.etRouterIp.setText(it.ip)
            binding.etRouterPort.setText(it.port.toString())
            binding.etRouterProtocol.setText(it.protocol)
            binding.etRouterLoginPath.setText(it.loginPath)
            binding.etUsernameSelector.setText(it.usernameSelector)
            binding.etPasswordSelector.setText(it.passwordSelector)
            binding.etSubmitSelector.setText(it.submitSelector)
            binding.etSuccessIndicator.setText(it.successIndicator)
            binding.etFailureIndicator.setText(it.failureIndicator)
            binding.etMd5Salt.setText(it.md5Salt)
            binding.etLogoutSelector.setText(it.logoutSelector)
        }
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
                protocol = binding.etRouterProtocol.text.toString(),
                loginPath = binding.etRouterLoginPath.text.toString(),
                usernameSelector = binding.etUsernameSelector.text.toString(),
                passwordSelector = binding.etPasswordSelector.text.toString(),
                submitSelector = binding.etSubmitSelector.text.toString(),
                successIndicator = binding.etSuccessIndicator.text.toString(),
                failureIndicator = binding.etFailureIndicator.text.toString(),
                md5Salt = binding.etMd5Salt.text.toString(),
                logoutSelector = binding.etLogoutSelector.text.toString()
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
