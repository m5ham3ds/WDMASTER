package com.wdmaster.app.presentation.test

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R
import com.wdmaster.app.databinding.FragmentTestBinding
import com.wdmaster.app.presentation.common.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TestFragment : BaseFragment<FragmentTestBinding>() {

    private val viewModel: TestViewModel by viewModel()
    private var webView: WebView? = null

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.wdmaster.app.action.PAUSE" -> viewModel.pauseTest()
                "com.wdmaster.app.action.RESUME" -> viewModel.resumeTest()
                "com.wdmaster.app.action.CANCEL" -> viewModel.cancelTest()
            }
        }
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentTestBinding.inflate(inflater, container, false)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = binding.webView
        setupWebView()

        lifecycleScope.launch {
            viewModel.testState.collectLatest { state -> updateOverlay(state) }
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

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.isTestRunning()) {
                        viewModel.requestCancelTest()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        )

        arguments?.let { args ->
            val routerId = args.getLong("routerId", 0L)
            val cardList = args.getStringArrayList("cardList") ?: arrayListOf()
            val delayMs = args.getLong("delayMs", 500L)
            if (routerId != 0L && cardList.isNotEmpty() && webView != null) {
                viewModel.startTest(routerId, cardList, delayMs, webView!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction("com.wdmaster.app.action.PAUSE")
            addAction("com.wdmaster.app.action.RESUME")
            addAction("com.wdmaster.app.action.CANCEL")
        }
        requireContext().registerReceiver(actionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(actionReceiver)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.allowFileAccess = false
            settings.allowContentAccess = false

            webViewClient = RouterWebViewClient(viewModel)
            webChromeClient = RouterChromeClient(viewModel)
            addJavascriptInterface(JavaScriptInterface(viewModel), "AndroidBridge")
        }
    }

    private fun updateOverlay(state: TestStateMachine.State) {
        binding.tvStateOverlay.apply {
            text = when (state) {
                TestStateMachine.State.IDLE -> getString(R.string.state_idle)
                TestStateMachine.State.LOADING_PAGE -> getString(R.string.state_loading)
                TestStateMachine.State.WAITING_DOM -> getString(R.string.state_loading)
                TestStateMachine.State.INJECTING_CARD -> getString(R.string.state_injecting)
                TestStateMachine.State.SUBMITTING_LOGIN -> getString(R.string.state_loading)
                TestStateMachine.State.CHECKING_RESULT -> getString(R.string.state_checking)
                TestStateMachine.State.SUCCESS -> getString(R.string.state_success)
                TestStateMachine.State.FAILURE -> getString(R.string.state_failure)
                TestStateMachine.State.RETRY -> "Retrying..."
                TestStateMachine.State.LOGOUT -> "Logging out..."
            }
            visibility = if (state == TestStateMachine.State.IDLE) View.GONE else View.VISIBLE
        }
        binding.progressLoading.visibility =
            if (state == TestStateMachine.State.LOADING_PAGE || state == TestStateMachine.State.WAITING_DOM ||
                state == TestStateMachine.State.SUBMITTING_LOGIN || state == TestStateMachine.State.CHECKING_RESULT
            ) View.VISIBLE else View.GONE
    }

    private fun showRetryDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(R.layout.dialog_retry)
            .apply {
                create().apply {
                    findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_retry_confirm)?.setOnClickListener {
                        viewModel.retryTest()
                        dismiss()
                    }
                    findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_retry_cancel)?.setOnClickListener {
                        viewModel.cancelTest()
                        dismiss()
                    }
                }.show()
            }
    }

    private fun showCancelDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setView(R.layout.dialog_cancel_test)
            .apply {
                create().apply {
                    findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel_yes)?.setOnClickListener {
                        viewModel.cancelTest()
                        dismiss()
                    }
                    findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel_no)?.setOnClickListener {
                        dismiss()
                    }
                }.show()
            }
    }

    override fun onDestroyView() {
        webView?.destroy()
        webView = null
        super.onDestroyView()
    }
}