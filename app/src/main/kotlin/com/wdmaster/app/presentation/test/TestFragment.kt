package com.wdmaster.app.presentation.test

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R
import com.wdmaster.app.databinding.FragmentTestBinding
import com.wdmaster.app.presentation.common.BaseFragment
import com.wdmaster.app.service.ServiceState
import com.wdmaster.app.service.TestService
import com.wdmaster.app.service.ServiceBinder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TestFragment : BaseFragment<FragmentTestBinding>() {

    private val viewModel: TestViewModel by viewModel()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) startTest() }

    private var serviceConnection: ServiceConnection? = null
    private var testService: TestService? = null
    private var isRetryDialogVisible = false

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentTestBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // مراقبة حالة الخدمة
        lifecycleScope.launch {
            viewModel.serviceState.collectLatest { state -> updateOverlay(state) }
        }
        lifecycleScope.launch {
            viewModel.uiEvent.collectLatest { event ->
                when (event) {
                    is TestViewModel.UiEvent.ShowRetryDialog -> showRetryDialog(event.message)
                    is TestViewModel.UiEvent.ShowCancelDialog -> showCancelDialog()
                    is TestViewModel.UiEvent.NavigateBack -> requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        binding.btnPause.setOnClickListener { viewModel.pauseTest() }
        binding.btnResume.setOnClickListener { viewModel.resumeTest() }
        binding.btnCancel.setOnClickListener { viewModel.requestCancelTest() }

        binding.btnPause.visibility = View.GONE
        binding.btnResume.visibility = View.GONE
        binding.btnCancel.visibility = View.GONE

        startTest()
    }

    private fun updateOverlay(state: ServiceState?) {
        // عرض اللقطة إذا وجدت
        if (state?.screenshot != null) {
            binding.ivScreenshot.setImageBitmap(state.screenshot)
            binding.ivScreenshot.visibility = View.VISIBLE
        } else {
            binding.ivScreenshot.visibility = View.GONE
        }

        if (state == null || state.status == "IDLE" || state.status == "STOPPED") {
            binding.tvStateOverlay.visibility = View.GONE
            binding.progressLoading.visibility = View.GONE
            binding.btnPause.visibility = View.GONE
            binding.btnResume.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            return
        }

        // أزرار التحكم
        binding.btnPause.visibility = if (state.status == "RUNNING") View.VISIBLE else View.GONE
        binding.btnResume.visibility = if (state.status == "PAUSED") View.VISIBLE else View.GONE
        binding.btnCancel.visibility = if (state.status in listOf("RUNNING", "PAUSED", "LOAD_ERROR")) View.VISIBLE else View.GONE

        // نص الحالة
        val progressPercent = if (state.total > 0) "${(state.progress * 100) / state.total}%" else ""
        val text = buildString {
            appendLine("الحالة: ${when (state.status) {
                "RUNNING" -> "قيد التشغيل"
                "PAUSED" -> "متوقف مؤقتاً"
                "LOAD_ERROR" -> "خطأ تحميل"
                else -> state.status
            }}")
            appendLine("البطاقة الحالية: ${state.currentCard}")
            appendLine("التقدم: ${state.progress}/${state.total} ($progressPercent)")
            appendLine("الناجح: ${state.successCount} | الفاشل: ${state.failureCount}")
            if (state.error != null && state.error != "LOAD_ERROR") appendLine("خطأ: ${state.error}")
        }
        binding.tvStateOverlay.text = text
        binding.tvStateOverlay.visibility = View.VISIBLE
        binding.progressLoading.visibility = if (state.status == "RUNNING") View.VISIBLE else View.GONE

        // حوار خطأ التحميل
        if (state.status == "LOAD_ERROR" && !isRetryDialogVisible) {
            isRetryDialogVisible = true
            showRetryDialog(state.error ?: "فشل تحميل الصفحة")
        }
        if (state.status != "LOAD_ERROR") isRetryDialogVisible = false
    }

    private fun startTest() {
        arguments?.let { args ->
            val routerId = args.getLong("routerId", 0L)
            val cardList = args.getStringArrayList("cardList") ?: arrayListOf()
            val delayMs = args.getLong("delayMs", 500L)
            if (routerId != 0L && cardList.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return
                    }
                }
                viewModel.saveTestConfig(routerId, cardList, delayMs)
                val intent = Intent(requireContext(), TestService::class.java).apply {
                    action = TestService.ACTION_START
                    putExtra(TestService.EXTRA_ROUTER_ID, routerId)
                    putStringArrayListExtra(TestService.EXTRA_CARD_LIST, ArrayList(cardList))
                    putExtra(TestService.EXTRA_DELAY_MS, delayMs)
                }
                requireContext().startService(intent)
                bindToService(intent)
            }
        }
    }

    private fun bindToService(intent: Intent) {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                testService = (service as ServiceBinder).getService()
                lifecycleScope.launch {
                    testService?.serviceState?.collect { state -> viewModel.updateServiceState(state) }
                }
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                testService = null
                viewModel.updateServiceState(null)
            }
        }
        requireContext().bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        serviceConnection?.let { requireContext().unbindService(it) }
        serviceConnection = null
    }

    private fun showRetryDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setTitle("خطأ تحميل")
            .setMessage(message)
            .setPositiveButton("إعادة تحميل") { _, _ -> viewModel.retryLoad() }
            .setNegativeButton("إلغاء الاختبار") { _, _ -> viewModel.cancelTest() }
            .setCancelable(false)
            .show()
    }

    private fun showCancelDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setTitle("إلغاء الاختبار")
            .setMessage("هل أنت متأكد من إلغاء الاختبار؟")
            .setPositiveButton("نعم") { _, _ -> viewModel.cancelTest() }
            .setNegativeButton("لا", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
