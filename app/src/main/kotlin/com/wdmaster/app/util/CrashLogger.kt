package com.wdmaster.app.util

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

class CrashLogger private constructor(private val context: Context) :
    Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        saveCrashLog(throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun saveCrashLog(throwable: Throwable) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val fileName = "crash_$timestamp.txt"

            val sw = StringWriter()
            PrintWriter(sw).use { throwable.printStackTrace(it) }
            val content = """
                Time: ${Date()}
                Exception: ${throwable.javaClass.name}
                Message: ${throwable.message}
                
                Stack Trace:
                ${sw.toString()}
            """.trimIndent()

            // حفظ في المسار الداخلي
            val internalDir = File(context.filesDir, "crash_logs").also { it.mkdirs() }
            File(internalDir, fileName).writeText(content)

            // حفظ نسخة في المسار الخارجي الخاص بالتطبيق (يمكن الوصول إليه عبر USB)
            val externalDir = File(context.getExternalFilesDir(null), "crash_logs").also { it.mkdirs() }
            File(externalDir, fileName).writeText(content)

        } catch (_: Exception) {}
    }

    companion object {
        fun init(context: Context) {
            if (Thread.getDefaultUncaughtExceptionHandler() !is CrashLogger) {
                Thread.setDefaultUncaughtExceptionHandler(CrashLogger(context.applicationContext))
            }
        }
    }
}
