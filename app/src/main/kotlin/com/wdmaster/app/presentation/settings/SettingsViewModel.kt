package com.wdmaster.app.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.local.preferences.ThemePreferences
import com.wdmaster.app.data.repository.SettingsRepository
import com.wdmaster.app.presentation.common.BaseViewModel
import com.wdmaster.app.util.LocaleHelper
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val themePreferences: ThemePreferences
) : BaseViewModel() {

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
        // يمكن استخدام NavController هنا، لكن نتركه للـ Fragment
    }

    fun clearHistory() {
        viewModelScope.launch {
            // تنفيذ مسح النتائج (يُضاف لاحقاً)
        }
    }

    fun exportDatabase() {
        viewModelScope.launch {
            // تنفيذ تصدير قاعدة البيانات
        }
    }

    fun openGitHub() {
        // فتح رابط GitHub في المتصفح
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/m5ham/WDMASTER"))
        // تحتاج إلى context، لذا يمكن تمرير الحدث عبر UiEvent إن لزم
    }
}
