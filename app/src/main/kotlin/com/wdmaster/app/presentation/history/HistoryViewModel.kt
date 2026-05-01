package com.wdmaster.app.presentation.history

import android.os.Environment
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.local.entity.TestResultEntity
import com.wdmaster.app.data.local.entity.TestSessionEntity
import com.wdmaster.app.data.repository.SessionRepository
import com.wdmaster.app.data.repository.TestResultRepository
import com.wdmaster.app.domain.usecase.ExportResultsUseCase
import com.wdmaster.app.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class HistoryViewModel(
    private val testResultRepository: TestResultRepository,
    private val sessionRepository: SessionRepository,
    private val exportResultsUseCase: ExportResultsUseCase
) : BaseViewModel() {

    private val _sessions = MutableStateFlow<List<TestSessionEntity>>(emptyList())
    val sessions: StateFlow<List<TestSessionEntity>> = _sessions.asStateFlow()

    private val _selectedSessionId = MutableStateFlow<Long?>(null)
    private val _allResults = MutableStateFlow<List<TestResultEntity>>(emptyList())
    private val _currentFilter = MutableStateFlow("all")

    val filteredResults: StateFlow<List<TestResultEntity>> = combine(
        _allResults, _currentFilter
    ) { results, filter ->
        when (filter) {
            "success" -> results.filter { it.state == "Success" }
            "failure" -> results.filter { it.state == "Failure" || it.state == "Connection Error" || it.state == "Unknown" }
            else -> results
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            sessionRepository.allSessions.collect { list ->
                _sessions.value = list
            }
        }
    }

    fun selectSession(sessionId: Long) {
        _selectedSessionId.value = sessionId
        viewModelScope.launch {
            testResultRepository.getResultsBySession(sessionId).collect { list ->
                _allResults.value = list
            }
        }
    }

    fun applyFilter(status: String) {
        _currentFilter.value = status
    }

    fun exportToFile(fileName: String) {
        viewModelScope.launch {
            try {
                val dir = File(Environment.getExternalStorageDirectory(), "WIFITON")
                if (!dir.exists()) dir.mkdirs()
                val result = exportResultsUseCase.invoke(dir, fileName)
                when (result) {
                    is ExportResultsUseCase.Result.Success -> {
                        // يمكن إرسال حدث نجاح
                    }
                    is ExportResultsUseCase.Result.Error -> {
                        // يمكن إرسال حدث خطأ
                    }
                }
            } catch (e: Exception) {
                // معالجة الخطأ
            }
        }
    }
}
