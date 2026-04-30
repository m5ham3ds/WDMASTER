package com.wdmaster.app.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatTimestamp(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }

    fun currentTimestamp(): Long = System.currentTimeMillis()
}