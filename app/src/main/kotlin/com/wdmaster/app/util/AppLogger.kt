package com.wdmaster.app.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

object AppLogger {

    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private var initialized = false

    fun init(context: Context) {
        try {
            val logDir = File(context.filesDir, "app_logs")
            if (!logDir.exists()) logDir.mkdirs()
            logFile = File(logDir, "app_logs.txt")
            initialized = true
            i("AppLogger", "=== Session started === (Path: ${logFile?.absolutePath})")
            Log.i("AppLogger", "Log file ready at: ${logFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e("AppLogger", "Failed to init log file", e)
        }
    }

    @Synchronized
    fun log(level: String, tag: String, message: String, throwable: Throwable? = null) {
        // طباعة إلى Logcat دائماً
        val msg = "[$tag] $message"
        when (level) {
            "ERROR" -> Log.e(tag, msg, throwable)
            "WARNING" -> Log.w(tag, msg, throwable)
            "DEBUG" -> Log.d(tag, msg, throwable)
            else -> Log.i(tag, msg, throwable)
        }

        // الكتابة إلى الملف إذا كان جاهزاً
        if (!initialized || logFile == null) return
        try {
            val timestamp = dateFormat.format(Date())
            val sb = StringBuilder()
            sb.appendLine("[$timestamp] [$level] [$tag] $message")
            if (throwable != null) {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                sb.appendLine(sw.toString())
            }
            sb.appendLine("---")
            logFile?.appendText(sb.toString())
        } catch (_: Exception) {}
    }

    fun d(tag: String, message: String) = log("DEBUG", tag, message)
    fun i(tag: String, message: String) = log("INFO", tag, message)
    fun w(tag: String, message: String) = log("WARNING", tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) = log("ERROR", tag, message, throwable)
}
