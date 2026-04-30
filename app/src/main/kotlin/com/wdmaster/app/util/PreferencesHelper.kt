package com.wdmaster.app.util

import android.content.Context
import com.wdmaster.app.R

object PreferencesHelper {

    // تم حذف applyTheme(mode: String) من هنا

    fun getCurrentThemeLabel(context: Context, mode: String): String {
        return when (mode) {
            "light" -> context.getString(R.string.theme_light)
            "dark" -> context.getString(R.string.theme_dark)
            else -> context.getString(R.string.theme_system_default)
        }
    }
}