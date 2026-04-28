package com.wdmaster.app.util

import timber.log.Timber

class Logger(private val tag: String) {

    fun d(message: String) = Timber.tag(tag).d(message)
    fun i(message: String) = Timber.tag(tag).i(message)
    fun w(message: String) = Timber.tag(tag).w(message)
    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) Timber.tag(tag).e(throwable, message)
        else Timber.tag(tag).e(message)
    }
}