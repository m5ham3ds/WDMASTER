package com.wdmaster.app.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.local.preferences.ThemePreferences
import com.wdmaster.app.data.repository.SettingsRepository
import com.wdmaster.app.data.repository.TestResultRepository
import com.wdmaster.app.data.repository.SessionRepository
import com.wdmaster.app.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val themePreferences: ThemePreferences,
    private val testResultRepository: TestResultRepository,
    private val sessionRepository: SessionRepository
) : BaseViewModel() {

    sealed class UiEvent {
        object NavigateToRouterManager : UiEvent()
        object ShowClearHistoryDialog : UiEvent()
        data class ShowMessage(val message: String) : UiEvent()
        object OpenGitHub : UiEvent()
        object ExportDatabase : UiEvent()
    }

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setAppLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setAppLanguage(language)
        }
    }

    fun navigateToRouterManager() {
        viewModelScope.launch { _uiEvent.emit(UiEvent.NavigateToRouterManager) }
    }

    fun clearHistory() {
        viewModelScope.launch { _uiEvent.emit(UiEvent.ShowClearHistoryDialog) }
    }

    fun confirmClearHistory() {
        viewModelScope.launch {
            try {
                testResultRepository.deleteAll()
                sessionRepository.deleteAll()
                _uiEvent.emit(UiEvent.ShowMessage("تم مسح جميع السجلات بنجاح"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowMessage("فشل في مسح السجلات: ${e.message}"))
            }
        }
    }

    fun exportDatabase() {
        viewModelScope.launch { _uiEvent.emit(UiEvent.ExportDatabase) }
    }

    fun openGitHub() {
        viewModelScope.launch { _uiEvent.emit(UiEvent.OpenGitHub) }
    }
}
