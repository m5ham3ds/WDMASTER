package com.wdmaster.app.presentation.common

import androidx.appcompat.app.AppCompatDelegate
import com.wdmaster.app.data.local.preferences.ThemePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object ThemeManager {

    fun applyTheme(themePreferences: ThemePreferences) {
        val mode = runBlocking { themePreferences.themeMode.first() }
        applyTheme(mode)
    }

    fun applyTheme(mode: String) {
        val nightMode = when (mode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}