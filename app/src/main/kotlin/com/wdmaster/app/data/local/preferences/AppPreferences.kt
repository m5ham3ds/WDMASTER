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

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

class AppPreferences(private val context: Context) {

    private val dataStore = context.appDataStore

    companion object {
        val KEY_VIBRATE = booleanPreferencesKey(Constants.PREF_VIBRATE_ON_SUCCESS)
        val KEY_SOUND = booleanPreferencesKey(Constants.PREF_SOUND_ON_SUCCESS)
        val KEY_AUTO_EXPORT = booleanPreferencesKey(Constants.PREF_AUTO_EXPORT)
        val KEY_THREAD_COUNT = intPreferencesKey(Constants.PREF_THREAD_COUNT)
        val KEY_DEFAULT_ROUTER_ID = longPreferencesKey(Constants.PREF_DEFAULT_ROUTER_ID)
        val KEY_APP_LANGUAGE = stringPreferencesKey(Constants.PREF_APP_LANGUAGE)
    }

    val vibrateOnSuccess: Flow<Boolean> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs -> prefs[KEY_VIBRATE] ?: true }

    val soundOnSuccess: Flow<Boolean> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs -> prefs[KEY_SOUND] ?: false }

    val autoExport: Flow<Boolean> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs -> prefs[KEY_AUTO_EXPORT] ?: false }

    val threadCount: Flow<Int> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs -> prefs[KEY_THREAD_COUNT] ?: 3 }

    val defaultRouterId: Flow<Long> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs -> prefs[KEY_DEFAULT_ROUTER_ID] ?: 0L }

    val appLanguage: Flow<String> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs -> prefs[KEY_APP_LANGUAGE] ?: Constants.LANG_SYSTEM }

    suspend fun setVibrate(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_VIBRATE] = enabled }
    }

    suspend fun setSound(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_SOUND] = enabled }
    }

    suspend fun setAutoExport(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_AUTO_EXPORT] = enabled }
    }

    suspend fun setThreadCount(count: Int) {
        dataStore.edit { prefs -> prefs[KEY_THREAD_COUNT] = count }
    }

    suspend fun setDefaultRouterId(id: Long) {
        dataStore.edit { prefs -> prefs[KEY_DEFAULT_ROUTER_ID] = id }
    }

    suspend fun setAppLanguage(language: String) {
        dataStore.edit { prefs -> prefs[KEY_APP_LANGUAGE] = language }
    }
}