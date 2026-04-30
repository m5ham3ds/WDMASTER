package com.wdmaster.app.presentation.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.data.repository.SessionRepository
import com.wdmaster.app.data.repository.TestResultRepository
import com.wdmaster.app.domain.model.*
import com.wdmaster.app.domain.usecase.GenerateCardsUseCase
import com.wdmaster.app.presentation.common.BaseViewModel
import com.wdmaster.app.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class ConnectionState { CONNECTED, DISCONNECTED }

class HomeViewModel(
    private val generateCardsUseCase: GenerateCardsUseCase,
    private val sessionRepository: SessionRepository,
    private val routerRepository: RouterRepository,
    private val testResultRepository: TestResultRepository
) : BaseViewModel() {

    val routers: StateFlow<List<RouterProfileEntity>> = routerRepository.allRouters
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedRouterId = MutableStateFlow(0L)
    val selectedRouterId: StateFlow<Long> = _selectedRouterId.asStateFlow()

    private val _prefix = MutableStateFlow("")
    private val _length = MutableStateFlow(Constants.DEFAULT_LENGTH)
    private val _count = MutableStateFlow(Constants.DEFAULT_COUNT)
    private val _delay = MutableStateFlow(Constants.DEFAULT_DELAY_MS)
    private val _charset = MutableStateFlow(Constants.CHARSET_NUMERIC)

    val length: StateFlow<Int> = _length.asStateFlow()
    val prefix: StateFlow<String> = _prefix.asStateFlow()
    val count: StateFlow<Int> = _count.asStateFlow()
    val charset: StateFlow<String> = _charset.asStateFlow()

    private val _statistics = MutableStateFlow(Statistics())
    val statistics: StateFlow<Statistics> = _statistics.asStateFlow()

    private val _logEntries = MutableStateFlow<List<LogEntry>>(emptyList())
    val logEntries: StateFlow<List<LogEntry>> = _logEntries.asStateFlow()

    sealed class UiState {
        object Idle : UiState()
        object Testing : UiState()
        data class Finished(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun observeLatestSession() {
        viewModelScope.launch {
            sessionRepository.allSessions.collect { sessions ->
                val latest = sessions.firstOrNull()
                if (latest != null && latest.isRunning) {
                    testResultRepository.getResultsBySession(latest.id).collect { results ->
                        val logs = results.map { entity ->
                            LogEntry(
                                level = if (entity.state == "Success") LogLevel.SUCCESS else LogLevel.ERROR,
                                message = entity.message,
                                timestamp = entity.testedAt
                            )
                        }.sortedBy { it.timestamp }
                        _logEntries.value = logs
                        updateStatsFromList(logs)
                        _uiState.value = UiState.Testing
                    }
                } else if (latest != null && !latest.isRunning) {
                    testResultRepository.getResultsBySession(latest.id).collect { results ->
                        val logs = results.map { entity ->
                            LogEntry(
                                level = if (entity.state == "Success") LogLevel.SUCCESS else LogLevel.ERROR,
                                message = entity.message,
                                timestamp = entity.testedAt
                            )
                        }.sortedBy { it.timestamp }
                        _logEntries.value = logs
                        updateStatsFromList(logs)
                        _uiState.value = UiState.Finished("Test completed")
                    }
                } else {
                    _logEntries.value = emptyList()
                    _statistics.value = Statistics()
                    _uiState.value = UiState.Idle
                }
            }
        }
    }

    private fun updateStatsFromList(logs: List<LogEntry>) {
        val tested = logs.count { it.level == LogLevel.SUCCESS || it.level == LogLevel.ERROR }
        val success = logs.count { it.level == LogLevel.SUCCESS }
        val failure = logs.count { it.level == LogLevel.ERROR }
        _statistics.value = Statistics(
            tested = tested,
            success = success,
            failure = failure,
            speed = if (tested > 0) tested / 10.0 else 0.0,
            eta = if (tested > 0 && _count.value > tested) ((_count.value - tested) * _delay.value) else 0,
            successRate = if (tested > 0) success.toFloat() / tested else 0f
        )
    }

    fun startMonitoringConnection(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _connectionState.value = ConnectionState.CONNECTED
            }
            override fun onLost(network: Network) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }
        cm.registerNetworkCallback(request, networkCallback!!)
        val currentNetwork = cm.activeNetwork
        if (currentNetwork != null) {
            val caps = cm.getNetworkCapabilities(currentNetwork)
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                _connectionState.value = ConnectionState.CONNECTED
            }
        }
    }

    fun stopMonitoringConnection(context: Context) {
        networkCallback?.let {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(it)
        }
    }

    fun selectRouter(id: Long) { _selectedRouterId.value = id }
    fun updateLength(length: Int) { _length.value = length }
    fun updateCount(count: Int) { _count.value = count }
    fun updatePrefix(prefix: String) { _prefix.value = prefix }
    fun updateDelay(delay: Long) { _delay.value = delay }
    fun updateCharset(charset: String) { _charset.value = charset }

    fun startRealTest(prefix: String, onTestReady: (Long, List<String>, Long) -> Unit) {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            _uiState.value = UiState.Error("يرجى الاتصال بشبكة واي فاي أولاً")
            return
        }
        val routerId = _selectedRouterId.value
        if (routerId == 0L) {
            _uiState.value = UiState.Error("Please select a router first")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val router = routerRepository.getRouterById(routerId)
            if (router == null) {
                withContext(Dispatchers.Main) {
                    _uiState.value = UiState.Error("Router not found")
                }
                return@launch
            }
            val cards = generateCardsUseCase(prefix, _length.value, _count.value, _charset.value)
            val cardCodes = cards.map { it.code }
            withContext(Dispatchers.Main) {
                onTestReady(routerId, cardCodes, _delay.value)
            }
        }
    }

    fun clearLog() {
        _logEntries.value = emptyList()
        _statistics.value = Statistics()
    }

    fun getLogText(): String = _logEntries.value.joinToString("\n") { "[${it.level}] ${it.message}" }
}