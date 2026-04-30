package com.wdmaster.app.util

/**
 * جميع ثوابت التطبيق – المسارات، الإعدادات الافتراضية، الحدود.
 */
object Constants {

    // ── Navigation Routes ──────────────────────────
    const val ROUTE_HOME = "home"
    const val ROUTE_TEST = "test"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_ROUTER_MANAGER = "router_manager"
    const val ROUTE_HISTORY = "history"

    // ── Bottom Navigation ──────────────────────────
    const val BOTTOM_NAV_HOME = 0
    const val BOTTOM_NAV_TEST = 1

    // ── Notification Channels ──────────────────────
    const val CHANNEL_TEST = "test_channel"
    const val CHANNEL_RESULT = "result_channel"
    const val NOTIFICATION_TEST_ID = 1001
    const val NOTIFICATION_RESULT_ID = 1002

    // ── Test Config Defaults ───────────────────────
    const val DEFAULT_PREFIX = ""
    const val DEFAULT_LENGTH = 8
    const val DEFAULT_COUNT = 50
    const val DEFAULT_DELAY_MS = 500L
    const val DEFAULT_RETRY = 3
    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 16
    const val MIN_COUNT = 1
    const val MAX_COUNT = 1000
    const val MIN_DELAY = 0L
    const val MAX_DELAY = 5000L
    const val MIN_RETRY = 1
    const val MAX_RETRY = 10

    // ── Character Sets ─────────────────────────────
    const val CHARSET_NUMERIC = "0123456789"
    const val CHARSET_HEX = "0123456789ABCDEF"
    const val CHARSET_ALPHA_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val CHARSET_ALPHA_LOWER = "abcdefghijklmnopqrstuvwxyz"
    const val CHARSET_ALL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    // ── Preferences Keys ───────────────────────────
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_APP_LANGUAGE = "app_language"
    const val PREF_VIBRATE_ON_SUCCESS = "vibrate_on_success"
    const val PREF_SOUND_ON_SUCCESS = "sound_on_success"
    const val PREF_AUTO_EXPORT = "auto_export"
    const val PREF_THREAD_COUNT = "thread_count"
    const val PREF_DEFAULT_ROUTER_ID = "default_router_id"

    // ── Language Codes ─────────────────────────────
    const val LANG_ARABIC = "ar"
    const val LANG_ENGLISH = "en"
    const val LANG_SYSTEM = "system"

    // ── File / Export ──────────────────────────────
    const val EXPORT_DIR = "WiFiCardMaster/exports"
    const val LOG_DIR = "WiFiCardMaster/logs"

    // ── Timeout & Limits ───────────────────────────
    const val WEBVIEW_TIMEOUT_MS = 15_000L
    const val FORM_READY_TIMEOUT_MS = 10_000L
    const val MAX_QUEUE_SIZE = 10_000
}