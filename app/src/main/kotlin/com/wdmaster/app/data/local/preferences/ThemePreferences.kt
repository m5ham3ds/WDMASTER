package com.wdmaster.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.wdmaster.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

class ThemePreferences(private val context: Context) {

    private val dataStore = context.themeDataStore

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey(Constants.PREF_THEME_MODE)
    }

    val themeMode: Flow<String> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs -> prefs[KEY_THEME_MODE] ?: "dark" } // الافتراضي Dark كما في البرومبت

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { prefs -> prefs[KEY_THEME_MODE] = mode }
    }
}