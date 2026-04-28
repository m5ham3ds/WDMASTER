package com.wdmaster.app.presentation.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.wdmaster.app.R
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.databinding.FragmentHomeBinding
import com.wdmaster.app.presentation.common.BaseFragment
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
        binding.switchSkipTested.setOnCheckedChangeListener { _, isChecked -> viewModel.updateSkipTested(isChecked) }
        binding.switchStopOnSuccess.setOnCheckedChangeListener { _, isChecked -> viewModel.updateStopOnSuccess(isChecked) }
    }

    private fun setupStatistics() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.statistics.collectLatest { stats ->
                binding.tvSentCount.text = stats.tested.toString()
                binding.tvSuccessCount.text = stats.success.toString()
                binding.tvFailedCount.text = stats.failure.toString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.successfulCount.collectLatest { count ->
                binding.btnSaveSuccessful.text = "حفظ الناجح ($count)"
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

            // الانتقال إلى TestFragment مع المعاملات
            viewModel.startRealTest(prefix) { routerId, cardList, delayMs ->
                findNavController().navigate(
                    R.id.nav_test_fragment,
                    bundleOf(
                        "routerId" to routerId,
                        "cardList" to ArrayList(cardList),
                        "delayMs" to delayMs
                    )
                )
            }
        }

        binding.btnStopTest.setOnClickListener { viewModel.stopTest() }

        binding.btnSaveSuccessful.setOnClickListener {
            viewModel.saveSuccessfulPatterns()
            Toast.makeText(requireContext(), "تم الحفظ!", Toast.LENGTH_SHORT).show()
        }

        binding.btnCopy.setOnClickListener {
            val logText = viewModel.getLogText()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("log", logText))
            Toast.makeText(requireContext(), "تم النسخ", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearLogExtra.setOnClickListener { viewModel.clearLog() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is HomeViewModel.UiState.Idle -> {
                        binding.progressBar.visibility = View.GONE; binding.tvProgress.text = "0%"
                        binding.btnStartTest.isEnabled = true; binding.btnStopTest.isEnabled = false
                    }
                    is HomeViewModel.UiState.Testing -> {
                        binding.progressBar.visibility = View.VISIBLE; binding.progressBar.progress = state.progress
                        binding.tvProgress.text = "${state.progress}%"
                        binding.btnStartTest.isEnabled = false; binding.btnStopTest.isEnabled = true
                    }
                    is HomeViewModel.UiState.Finished -> {
                        binding.progressBar.visibility = View.GONE; binding.tvProgress.text = "100%"
                        binding.btnStartTest.isEnabled = true; binding.btnStopTest.isEnabled = false
                        if (state.message.isNotEmpty()) Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    is HomeViewModel.UiState.Error -> {
                        binding.progressBar.visibility = View.GONE; binding.tvProgress.text = "0%"
                        binding.btnStartTest.isEnabled = true; binding.btnStopTest.isEnabled = false
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
        binding.recyclerLog.adapter = null
        super.onDestroyView()
    }
}