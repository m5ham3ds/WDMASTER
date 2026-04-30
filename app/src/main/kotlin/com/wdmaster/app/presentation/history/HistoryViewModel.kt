package com.wdmaster.app.presentation.history

import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.local.entity.TestResultEntity
import com.wdmaster.app.data.local.entity.TestSessionEntity
import com.wdmaster.app.data.repository.SessionRepository
import com.wdmaster.app.data.repository.TestResultRepository
import com.wdmaster.app.domain.usecase.ExportResultsUseCase
import com.wdmaster.app.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val testResultRepository: TestResultRepository,
    private val sessionRepository: SessionRepository,
    private val exportResultsUseCase: ExportResultsUseCase
) : BaseViewModel() {

    private val _sessions = MutableStateFlow<List<TestSessionEntity>>(emptyList())
    val sessions: StateFlow<List<TestSessionEntity>> = _sessions.asStateFlow()

    private val _selectedSessionId = MutableStateFlow<Long?>(null)
    private val _allResults = MutableStateFlow<List<TestResultEntity>>(emptyList())
    private val _currentFilter = MutableStateFlow("all") // all, success, failure

    val filteredResults: StateFlow<List<TestResultEntity>> = combine(
        _allResults, _currentFilter
    ) { results, filter ->
        when (filter) {
            "success" -> results.filter { it.state == "Success" }
            "failure" -> results.filter { it.state == "Failure" }
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
        // إعادة تطبيق على النتائج الحالية (تلقائي عبر combine)
    }

    fun exportToFile(fileName: String) {
        viewModelScope.launch {
            // نستخدم مجلد مؤقت داخلي
            val dir = java.io.File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "WiFiCardMaster")
            dir.mkdirs()
            val file = java.io.File(dir, fileName)
            exportResultsUseCase.invoke(file)
        }
    }
}