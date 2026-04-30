package com.wdmaster.app.domain.usecase

import com.wdmaster.app.data.local.entity.TestResultEntity
import com.wdmaster.app.data.repository.TestResultRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import java.io.File

class ExportResultsUseCase(private val testResultRepository: TestResultRepository) {

    sealed class Result {
        data class Success(val file: File) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(outputDir: File, fileName: String = "results_export.json"): Result {
        return try {
            val results = testResultRepository.allResults.first()
            val gson = GsonBuilder().setPrettyPrinting().create()
            val json = gson.toJson(results)

            if (!outputDir.exists()) outputDir.mkdirs()
            val file = File(outputDir, fileName)
            file.writeText(json)
            Result.Success(file)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Export failed")
        }
    }
}