package com.wdmaster.app.presentation.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.repository.CardRepository
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.data.repository.SessionRepository
import com.wdmaster.app.domain.model.*
import com.wdmaster.app.domain.usecase.ExportResultsUseCase
import com.wdmaster.app.domain.usecase.GenerateCardsUseCase
import com.wdmaster.app.domain.usecase.PatternLearningUseCase
import com.wdmaster.app.domain.usecase.TestBatchUseCase
import com.wdmaster.app.presentation.common.BaseViewModel
import com.wdmaster.app.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class ConnectionState { CONNECTED, DISCONNECTED }

class HomeViewModel(
    private val generateCardsUseCase: GenerateCardsUseCase,
    private val testBatchUseCase: TestBatchUseCase,
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository,
    private val patternLearningUseCase: PatternLearningUseCase,
    private val routerRepository: RouterRepository,
    private val exportResultsUseCase: ExportResultsUseCase
) : BaseViewModel() {

    val routers: StateFlow<List<RouterProfileEntity>> = routerRepository.allRouters
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedRouterId = MutableStateFlow(0L)

    private val _prefix = MutableStateFlow("")
    private val _length = MutableStateFlow(Constants.DEFAULT_LENGTH)
    private val _count = MutableStateFlow(Constants.DEFAULT_COUNT)
    private val _delay = MutableStateFlow(Constants.DEFAULT_DELAY_MS)
    private val _retry = MutableStateFlow(Constants.DEFAULT_RETRY)
    private val _charset = MutableStateFlow(Constants.CHARSET_NUMERIC)
    private val _skipTested = MutableStateFlow(true)
    private val _stopOnSuccess = MutableStateFlow(false)

    val length: StateFlow<Int> = _length.asStateFlow()
    val prefix: StateFlow<String> = _prefix.asStateFlow()
    val count: StateFlow<Int> = _count.asStateFlow()
    val charset: StateFlow<String> = _charset.asStateFlow()

    private val _statistics = MutableStateFlow(Statistics())
    val statistics: StateFlow<Statistics> = _statistics.asStateFlow()

    private val _successfulCount = MutableStateFlow(0)
    val successfulCount: StateFlow<Int> = _successfulCount.asStateFlow()

    private val _logEntries = MutableStateFlow<List<LogEntry>>(emptyList())
    val logEntries: StateFlow<List<LogEntry>> = _logEntries.asStateFlow()

    sealed class UiState {
        object Idle : UiState()
        data class Testing(val progress: Int) : UiState()
        data class Finished(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var testJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

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
    fun updateRetry(retry: Int) { _retry.value = retry }
    fun updateCharset(charset: String) { _charset.value = charset }
    fun updateSkipTested(skip: Boolean) { _skipTested.value = skip }
    fun updateStopOnSuccess(stop: Boolean) { _stopOnSuccess.value = stop }

    fun startRealTest(prefix: String, onTestReady: (Long, List<String>, Long) -> Unit) {
        val routerId = _selectedRouterId.value
        if (routerId == 0L) {
            _uiState.value = UiState.Error("Please select a router first")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val router = routerRepository.getRouterById(routerId)
            if (router == null) {
                _uiState.value = UiState.Error("Router not found")
                return@launch
            }
            val cards = generateCardsUseCase(prefix, _length.value, _count.value, _charset.value)
            val cardCodes = cards.map { it.code }
            withContext(Dispatchers.Main) {
                onTestReady(routerId, cardCodes, _delay.value)
            }
        }
    }

    fun startTest(prefix: String) {
        if (_selectedRouterId.value == 0L) {
            _uiState.value = UiState.Error("Please select a router first")
            return
        }
        testJob?.cancel()
        testJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Testing(0)
            addLog(LogLevel.INFO, "Starting test: prefix=$prefix, length=${_length.value}, count=${_count.value}")

            val config = TestConfig(
                routerId = _selectedRouterId.value,
                prefix = prefix,
                length = _length.value,
                count = _count.value,
                delayMs = _delay.value,
                retry = _retry.value,
                charset = _charset.value,
                skipTested = _skipTested.value,
                stopOnSuccess = _stopOnSuccess.value
            )

            try {
                testBatchUseCase(config).collect { state ->
                    when (state) {
                        is TestState.Success -> {
                            addLog(LogLevel.SUCCESS, state.message)
                            _successfulCount.value++
                            if (_stopOnSuccess.value) testJob?.cancel()
                        }
                        is TestState.Failure -> addLog(LogLevel.ERROR, state.reason)
                        else -> {}
                    }
                    updateStats()
                }
                _uiState.value = UiState.Finished("Test completed")
            } catch (e: CancellationException) {
                addLog(LogLevel.WARNING, "Test cancelled")
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                addLog(LogLevel.ERROR, "Test failed: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun stopTest() {
        testJob?.cancel()
        addLog(LogLevel.WARNING, "Test stopped by user")
        _uiState.value = UiState.Idle
    }

    fun clearLog() { _logEntries.value = emptyList() }

    fun saveSuccessfulPatterns() {
        viewModelScope.launch(Dispatchers.IO) {
            _logEntries.value.filter { it.level == LogLevel.SUCCESS }.forEach {
                patternLearningUseCase.learnFromSuccess(it.message, _selectedRouterId.value, _charset.value)
            }
        }
    }

    fun exportResults(): Boolean = try { true } catch (e: Exception) { false }

    fun getLogText(): String = _logEntries.value.joinToString("\n") { "[${it.level}] ${it.message}" }

    private fun addLog(level: LogLevel, message: String) {
        _logEntries.value = _logEntries.value + LogEntry(level = level, message = message)
    }

    private suspend fun updateStats() {
        val tested = _logEntries.value.count { it.level == LogLevel.SUCCESS || it.level == LogLevel.ERROR }
        val success = _logEntries.value.count { it.level == LogLevel.SUCCESS }
        val failure = _logEntries.value.count { it.level == LogLevel.ERROR }
        _statistics.value = Statistics(
            tested = tested,
            success = success,
            failure = failure,
            speed = if (tested > 0) tested / 10.0 else 0.0,
            eta = if (tested > 0 && _count.value > tested) ((_count.value - tested) * _delay.value) else 0,
            successRate = if (tested > 0) success.toFloat() / tested else 0f
        )
    }
}
