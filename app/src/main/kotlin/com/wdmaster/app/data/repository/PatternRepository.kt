package com.wdmaster.app.data.repository

import com.wdmaster.app.data.local.database.PatternDao
import com.wdmaster.app.data.local.entity.SuccessfulPatternEntity
import kotlinx.coroutines.flow.Flow

class PatternRepository(private val patternDao: PatternDao) {

    val allPatterns: Flow<List<SuccessfulPatternEntity>> = patternDao.getAllPatterns()

    fun getPatternsByRouter(routerId: Long): Flow<List<SuccessfulPatternEntity>> =
        patternDao.getPatternsByRouter(routerId)

    suspend fun insertPattern(pattern: SuccessfulPatternEntity) =
        patternDao.insertPattern(pattern)

    suspend fun deletePattern(pattern: SuccessfulPatternEntity) =
        patternDao.deletePattern(pattern)

    suspend fun deleteAll() = patternDao.deleteAll()
}