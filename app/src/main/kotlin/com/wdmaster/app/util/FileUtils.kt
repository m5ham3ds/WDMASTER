package com.wdmaster.app.util

import java.io.File

object FileUtils {

    fun ensureDirExists(dir: File): Boolean {
        return if (!dir.exists()) dir.mkdirs() else true
    }

    fun writeText(file: File, content: String) {
        ensureDirExists(file.parentFile!!)
        file.writeText(content)
    }

    fun readText(file: File): String = file.readText()
}