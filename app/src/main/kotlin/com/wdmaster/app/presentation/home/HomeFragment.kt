package com.wdmaster.app.presentation.home

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.databinding.FragmentHomeBinding
import com.wdmaster.app.presentation.common.BaseFragment
import com.wdmaster.app.service.TestService
import com.wdmaster.app.util.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModel()
    private lateinit var logAdapter: LogAdapter
    private var routerList = listOf<RouterProfileEntity>()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRouterSpinner()
        setupAdvancedToggle()
        setupStatistics()
        setupLog()
        setupButtons()
        observeViewModel()
        startMonitoringConnection()
        viewModel.observeLatestSession()
    }

    private fun startMonitoringConnection() {
        viewModel.startMonitoringConnection(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectionState.collectLatest { state ->
                val connected = state == ConnectionState.CONNECTED
                binding.btnStartTest.isEnabled = connected
                binding.tvConnectionStatus.text = if (connected) "🟢 متصل بالراوتر" else "🔴 غير متصل"
            }
        }
    }

    private fun setupRouterSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.routers.collectLatest { routers ->
                routerList = routers
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    routers.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerRouter.adapter = adapter
                if (routers.isNotEmpty()) {
                    binding.spinnerRouter.setSelection(0)
                    updateRouterInfo(routers[0])
                }
                binding.spinnerRouter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position < routers.size) {
                            viewModel.selectRouter(routers[position].id)
                            updateRouterInfo(routers[position])
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }

    private fun updateRouterInfo(router: RouterProfileEntity) {
        binding.tvRouterInfo.text = "${router.ip}:${router.port} (${router.protocol})"
    }

    private fun setupAdvancedToggle() {
        binding.advancedHeader.setOnClickListener {
            if (binding.advancedContent.visibility == View.VISIBLE) {
                binding.advancedContent.visibility = View.GONE
                binding.tvAdvancedToggle.text = "▼"
            } else {
                binding.advancedContent.visibility = View.VISIBLE
                binding.tvAdvancedToggle.text = "▲"
            }
        }
    }

    private fun setupStatistics() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.statistics.collectLatest { stats ->
                binding.tvSentCount.text = stats.tested.toString()
                binding.tvSuccessCount.text = stats.success.toString()
                binding.tvFailedCount.text = stats.failure.toString()
            }
        }
    }

    private fun setupLog() {
        logAdapter = LogAdapter()
        binding.recyclerLog.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLog.adapter = logAdapter
    }

    private fun setupButtons() {
        binding.btnStartTest.setOnClickListener {
            val length = binding.etLength.text?.toString()?.toIntOrNull()
            val prefix = binding.etPrefix.text?.toString() ?: ""
            val charset = binding.etCharset.text?.toString()?.ifEmpty { Constants.CHARSET_NUMERIC }
                ?: Constants.CHARSET_NUMERIC
            val count = binding.etCount.text?.toString()?.toIntOrNull()

            if (length == null || count == null) {
                Toast.makeText(requireContext(), "الرجاء إدخال قيم صحيحة للطول وعدد المحاولات", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateLength(length)
            viewModel.updatePrefix(prefix)
            viewModel.updateCharset(charset)
            viewModel.updateCount(count)

            // فحص وجود خدمة نشطة
            if (isServiceRunning(TestService::class.java)) {
                MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
                    .setTitle("اختبار جارٍ")
                    .setMessage("يوجد اختبار يعمل حالياً. هل تريد إيقافه وبدء اختبار جديد؟")
                    .setPositiveButton("نعم") { _, _ ->
                        val stopIntent = Intent(requireContext(), TestService::class.java).apply {
                            action = TestService.ACTION_CANCEL
                        }
                        requireContext().startService(stopIntent)
                        viewModel.startRealTest(prefix) { routerId, cardList, delayMs ->
                            findNavController().navigate(
                                R.id.nav_test_fragment,
                                bundleOf("routerId" to routerId, "cardList" to ArrayList(cardList), "delayMs" to delayMs)
                            )
                        }
                    }
                    .setNegativeButton("لا", null)
                    .show()
            } else {
                viewModel.startRealTest(prefix) { routerId, cardList, delayMs ->
                    findNavController().navigate(
                        R.id.nav_test_fragment,
                        bundleOf("routerId" to routerId, "cardList" to ArrayList(cardList), "delayMs" to delayMs)
                    )
                }
            }
        }

        binding.btnStopTest.setOnClickListener {
            val stopIntent = Intent(requireContext(), TestService::class.java).apply {
                action = TestService.ACTION_CANCEL
            }
            requireContext().startService(stopIntent)
        }

        binding.btnCopy.setOnClickListener {
            val logText = viewModel.getLogText()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("log", logText))
            Toast.makeText(requireContext(), "تم النسخ", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearLogExtra.setOnClickListener { viewModel.clearLog() }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is HomeViewModel.UiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvProgress.text = "0%"
                        binding.btnStartTest.isEnabled =
                            viewModel.connectionState.value == ConnectionState.CONNECTED
                        binding.btnStopTest.isEnabled = false
                    }
                    is HomeViewModel.UiState.Testing -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvProgress.text = ""
                        binding.btnStartTest.isEnabled = false
                        binding.btnStopTest.isEnabled = true
                    }
                    is HomeViewModel.UiState.Finished -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvProgress.text = "100%"
                        binding.btnStartTest.isEnabled =
                            viewModel.connectionState.value == ConnectionState.CONNECTED
                        binding.btnStopTest.isEnabled = false
                        if (state.message.isNotEmpty())
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    is HomeViewModel.UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvProgress.text = "0%"
                        binding.btnStartTest.isEnabled =
                            viewModel.connectionState.value == ConnectionState.CONNECTED
                        binding.btnStopTest.isEnabled = false
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.logEntries.collectLatest { logs ->
                logAdapter.submitList(logs)
                if (logs.isNotEmpty()) binding.recyclerLog.scrollToPosition(logs.size - 1)
            }
        }
    }

    override fun onDestroyView() {
        if (::logAdapter.isInitialized) {
            binding.recyclerLog.adapter = null
        }
        viewModel.stopMonitoringConnection(requireContext())
        super.onDestroyView()
    }
}