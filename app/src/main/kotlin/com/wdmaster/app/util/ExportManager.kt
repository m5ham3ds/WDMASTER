package com.wdmaster.app.util

import com.wdmaster.app.domain.model.TestResult
import com.google.gson.GsonBuilder
import java.io.File

class ExportManager {

    fun exportToJson(results: List<TestResult>, outputFile: File): Boolean {
        return try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val json = gson.toJson(results)
            FileUtils.writeText(outputFile, json)
            true
        } catch (e: Exception) {
            false
        }
    }
}