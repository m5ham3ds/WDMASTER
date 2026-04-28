package com.wdmaster.app.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wdmaster.app.databinding.FragmentHistoryBinding
import com.wdmaster.app.presentation.adapter.SessionAdapter
import com.wdmaster.app.presentation.adapter.TestResultAdapter
import com.wdmaster.app.presentation.common.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {

    private val viewModel: HistoryViewModel by viewModel()
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var resultAdapter: TestResultAdapter

    // حوارات
    private var filterDialog: FilterDialog? = null
    private var exportDialog: ExportDialog? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHistoryBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionAdapter = SessionAdapter { session -> viewModel.selectSession(session.id) }
        resultAdapter = TestResultAdapter()

        binding.recyclerSessions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionAdapter
        }
        binding.recyclerResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultAdapter
        }

        // مراقبة الجلسات
        lifecycleScope.launch {
            viewModel.sessions.collectLatest { sessions ->
                sessionAdapter.submitList(sessions)
            }
        }
        // مراقبة النتائج المُصفّاة
        lifecycleScope.launch {
            viewModel.filteredResults.collectLatest { results ->
                resultAdapter.submitList(results)
            }
        }

        // أزرار التصفية والتصدير
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
        binding.btnExport.setOnClickListener {
            showExportDialog()
        }
    }

    private fun showFilterDialog() {
        filterDialog = FilterDialog()
        filterDialog?.setOnFilterApplied { status ->
            viewModel.applyFilter(status)
        }
        filterDialog?.show(parentFragmentManager, "FilterDialog")
    }

    private fun showExportDialog() {
        exportDialog = ExportDialog()
        exportDialog?.setOnExportListener { fileName ->
            viewModel.exportToFile(fileName)
        }
        exportDialog?.show(parentFragmentManager, "ExportDialog")
    }
}