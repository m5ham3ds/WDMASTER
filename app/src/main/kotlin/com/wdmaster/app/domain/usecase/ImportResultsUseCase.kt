package com.wdmaster.app.domain.usecase

import com.wdmaster.app.data.local.entity.TestResultEntity
import com.wdmaster.app.data.repository.TestResultRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class ImportResultsUseCase(private val testResultRepository: TestResultRepository) {

    sealed class Result {
        data class Success(val count: Int) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(file: File): Result {
        return try {
            val json = file.readText()
            val type = object : TypeToken<List<TestResultEntity>>() {}.type
            val results: List<TestResultEntity> = Gson().fromJson(json, type)
            testResultRepository.insertResults(results)
            Result.Success(results.size)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Import failed")
        }
    }
}