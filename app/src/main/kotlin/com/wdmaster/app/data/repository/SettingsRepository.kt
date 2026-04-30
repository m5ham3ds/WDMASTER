package com.wdmaster.app.data.repository

import com.wdmaster.app.data.local.preferences.AppPreferences
import com.wdmaster.app.data.local.preferences.ThemePreferences
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val appPreferences: AppPreferences,
    private val themePreferences: ThemePreferences
) {
    val vibrateOnSuccess: Flow<Boolean> = appPreferences.vibrateOnSuccess
    val soundOnSuccess: Flow<Boolean> = appPreferences.soundOnSuccess
    val autoExport: Flow<Boolean> = appPreferences.autoExport
    val threadCount: Flow<Int> = appPreferences.threadCount
    val defaultRouterId: Flow<Long> = appPreferences.defaultRouterId
    val appLanguage: Flow<String> = appPreferences.appLanguage
    val themeMode: Flow<String> = themePreferences.themeMode

    suspend fun setVibrate(enabled: Boolean) = appPreferences.setVibrate(enabled)
    suspend fun setSound(enabled: Boolean) = appPreferences.setSound(enabled)
    suspend fun setAutoExport(enabled: Boolean) = appPreferences.setAutoExport(enabled)
    suspend fun setThreadCount(count: Int) = appPreferences.setThreadCount(count)
    suspend fun setDefaultRouterId(id: Long) = appPreferences.setDefaultRouterId(id)
    suspend fun setAppLanguage(language: String) = appPreferences.setAppLanguage(language)
    suspend fun setThemeMode(mode: String) = themePreferences.setThemeMode(mode)
}