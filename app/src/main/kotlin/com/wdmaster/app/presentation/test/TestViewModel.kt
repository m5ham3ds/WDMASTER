package com.wdmaster.app.presentation.test

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.service.NotificationHelper
import com.wdmaster.app.service.TestService
import com.wdmaster.app.service.ServiceState
import com.wdmaster.app.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TestViewModel(
    private val app: Application
) : AndroidViewModel(app) {

    val logger: Logger = Logger("TestViewModel")
    private val notificationHelper = NotificationHelper(app)

    private val _serviceState = MutableStateFlow<ServiceState?>(null)
    val serviceState: StateFlow<ServiceState?> = _serviceState.asStateFlow()

    private var lastRouter: RouterProfileEntity? = null
    private var lastCardList = listOf<String>()
    private var lastDelayMs = 500L
    private var lastRouterId: Long = 0L

    sealed class UiEvent {
        data class ShowRetryDialog(val message: String) : UiEvent()
        object ShowCancelDialog : UiEvent()
        object NavigateBack : UiEvent()
    }
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun updateServiceState(state: ServiceState?) {
        _serviceState.value = state
    }

    fun saveTestConfig(routerId: Long, cardList: List<String>, delayMs: Long) {
        lastRouterId = routerId
        lastCardList = cardList
        lastDelayMs = delayMs
    }

    fun pauseTest() {
        sendAction(TestService.ACTION_PAUSE)
    }

    fun resumeTest() {
        sendAction(TestService.ACTION_RESUME)
    }

    fun cancelTest() {
        sendAction(TestService.ACTION_CANCEL)
        _serviceState.value = null
        notificationHelper.cancelTestNotification()
    }

    fun retryLoad() {
        sendAction(TestService.ACTION_RETRY)
    }

    fun retryTest() {
        val intent = Intent(app, TestService::class.java).apply {
            action = TestService.ACTION_START
            putExtra(TestService.EXTRA_ROUTER_ID, lastRouterId)
            putStringArrayListExtra(TestService.EXTRA_CARD_LIST, ArrayList(lastCardList))
            putExtra(TestService.EXTRA_DELAY_MS, lastDelayMs)
        }
        app.startService(intent)
    }

    fun requestCancelTest() {
        viewModelScope.launch { _uiEvent.emit(UiEvent.ShowCancelDialog) }
    }

    fun isTestActive(): Boolean {
        val state = _serviceState.value
        return state != null && (state.status == "RUNNING" || state.status == "PAUSED" || state.status == "LOAD_ERROR")
    }

    private fun sendAction(action: String) {
        val intent = Intent(app, TestService::class.java).apply { this.action = action }
        app.startService(intent)
    }
}