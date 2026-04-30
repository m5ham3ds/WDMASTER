package com.wdmaster.app.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String) {
        val locale = when (languageCode) {
            Constants.LANG_ARABIC -> Locale("ar")
            Constants.LANG_ENGLISH -> Locale("en")
            else -> Resources.getSystem().configuration.locales.get(0)
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getPersistedLocale(context: Context): String {
        // يمكن قراءتها من DataStore، حالياً نعيد النظام
        return Constants.LANG_SYSTEM
    }

    fun onConfigurationChanged(context: Context) {}
}
