package com.wdmaster.app.domain.usecase

import com.wdmaster.app.data.local.entity.SuccessfulPatternEntity
import com.wdmaster.app.data.repository.PatternRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PatternLearningUseCase(private val patternRepository: PatternRepository) {

    val allPatterns: Flow<List<SuccessfulPatternEntity>> = patternRepository.allPatterns

    suspend fun learnFromSuccess(cardCode: String, routerId: Long, charset: String) {
        val pattern = SuccessfulPatternEntity(
            code = cardCode,
            routerId = routerId,
            charset = charset,
            length = cardCode.length
        )
        patternRepository.insertPattern(pattern)
    }

    suspend fun getPatternsByRouter(routerId: Long): List<SuccessfulPatternEntity> {
        return patternRepository.getPatternsByRouter(routerId).first()
    }

    suspend fun clearAll() = patternRepository.deleteAll()
}