package com.wdmaster.app.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.databinding.FragmentRouterManagerBinding
import com.wdmaster.app.presentation.adapter.RouterProfileAdapter
import com.wdmaster.app.presentation.common.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RouterManagerFragment : BaseFragment<FragmentRouterManagerBinding>() {

    private val viewModel: RouterManagerViewModel by viewModel()
    private lateinit var adapter: RouterProfileAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentRouterManagerBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RouterProfileAdapter(
            onTestClick = { router -> viewModel.testRouter(router) },
            onEditClick = { router -> showEditDialog(router) },
            onDeleteClick = { router -> showDeleteConfirmation(router) }
        )

        binding.recyclerRouters.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RouterManagerFragment.adapter
        }

        binding.fabAddRouter.setOnClickListener { showAddDialog() }

        lifecycleScope.launch {
            viewModel.routers.collectLatest { list -> adapter.submitList(list) }
        }
        lifecycleScope.launch {
            viewModel.uiEvent.collectLatest { event ->
                when (event) {
                    is RouterManagerViewModel.UiEvent.NavigateToTest -> {
                        // التنقل عبر NavController
                    }
                    is RouterManagerViewModel.UiEvent.ShowMessage -> {
                        // عرض رسالة
                    }
                }
            }
        }
    }

    private fun showAddDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(R.layout.dialog_add_router)
            .apply {
                val dialog = create()
                dialog.setOnShowListener {
                    dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save)?.setOnClickListener {
                        val router = RouterProfileEntity(
                            name = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_name)?.text.toString(),
                            ip = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_ip)?.text.toString(),
                            port = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_port)?.text?.toString()?.toIntOrNull() ?: 80,
                            protocol = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_protocol)?.text.toString(),
                            loginPath = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_login_path)?.text.toString(),
                            usernameSelector = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_username_selector)?.text.toString(),
                            passwordSelector = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_password_selector)?.text.toString(),
                            submitSelector = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_submit_selector)?.text.toString(),
                            successIndicator = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_success_indicator)?.text.toString(),
                            failureIndicator = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_failure_indicator)?.text.toString(),
                            md5Salt = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_md5_salt)?.text.toString(),
                            logoutSelector = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_logout_selector)?.text.toString()
                        )
                        viewModel.addRouter(router)
                        dialog.dismiss()
                    }
                    dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)?.setOnClickListener { dialog.dismiss() }
                }
                dialog.show()
            }
    }

    private fun showEditDialog(router: RouterProfileEntity) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(R.layout.dialog_edit_router)
            .apply {
                val dialog = create()
                dialog.setOnShowListener {
                    // تعبئة الحقول بالقيم الحالية
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_name_edit)?.setText(router.name)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_ip_edit)?.setText(router.ip)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_port_edit)?.setText(router.port.toString())
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_protocol_edit)?.setText(router.protocol)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_login_path_edit)?.setText(router.loginPath)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_username_selector_edit)?.setText(router.usernameSelector)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_password_selector_edit)?.setText(router.passwordSelector)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_submit_selector_edit)?.setText(router.submitSelector)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_success_indicator_edit)?.setText(router.successIndicator)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_failure_indicator_edit)?.setText(router.failureIndicator)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_md5_salt_edit)?.setText(router.md5Salt)
                    dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_logout_selector_edit)?.setText(router.logoutSelector)

                    dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save_edit)?.setOnClickListener {
                        val updated = router.copy(
                            name = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_name_edit)?.text.toString(),
                            ip = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_ip_edit)?.text.toString(),
                            port = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_port_edit)?.text?.toString()?.toIntOrNull() ?: 80,
                            protocol = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_protocol_edit)?.text.toString(),
                            loginPath = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_router_login_path_edit)?.text.toString(),
                            usernameSelector = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_username_selector_edit)?.text.toString(),
                            passwordSelector = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_password_selector_edit)?.text.toString(),
                            submitSelector = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_submit_selector_edit)?.text.toString(),
                            successIndicator = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_success_indicator_edit)?.text.toString(),
                            failureIndicator = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_failure_indicator_edit)?.text.toString(),
                            md5Salt = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_md5_salt_edit)?.text.toString(),
                            logoutSelector = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_logout_selector_edit)?.text.toString()
                        )
                        viewModel.updateRouter(updated)
                        dialog.dismiss()
                    }
                    dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel_edit)?.setOnClickListener { dialog.dismiss() }
                }
                dialog.show()
            }
    }

    private fun showDeleteConfirmation(router: RouterProfileEntity) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setTitle(getString(R.string.delete_router))
            .setMessage("هل أنت متأكد من حذف ${router.name}؟")
            .setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.deleteRouter(router) }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}
