package com.wdmaster.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.wdmaster.app.R

object PreferencesHelper {

    fun applyTheme(mode: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    fun getCurrentThemeLabel(context: Context, mode: String): String {
        return when (mode) {
            "light" -> context.getString(R.string.theme_light)
            "dark" -> context.getString(R.string.theme_dark)
            else -> context.getString(R.string.theme_system_default)
        }
    }
}
