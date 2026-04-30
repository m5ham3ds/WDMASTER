package com.wdmaster.app.util

import com.wdmaster.app.domain.model.TestResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class ImportManager {

    fun importFromJson(file: File): List<TestResult> {
        val json = file.readText()
        val type = object : TypeToken<List<TestResult>>() {}.type
        return Gson().fromJson(json, type)
    }
}