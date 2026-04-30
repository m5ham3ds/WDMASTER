package com.wdmaster.app.util

import timber.log.Timber

class Logger(private val tag: String) {

    fun d(message: String) {
        Timber.tag(tag).d(message)
        AppLogger.d(tag, message)
    }

    fun i(message: String) {
        Timber.tag(tag).i(message)
        AppLogger.i(tag, message)
    }

    fun w(message: String) {
        Timber.tag(tag).w(message)
        AppLogger.w(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, message)
            AppLogger.e(tag, message, throwable)
        } else {
            Timber.tag(tag).e(message)
            AppLogger.e(tag, message)
        }
    }
}