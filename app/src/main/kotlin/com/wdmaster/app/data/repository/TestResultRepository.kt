package com.wdmaster.app.data.repository

import com.wdmaster.app.data.local.database.TestResultDao
import com.wdmaster.app.data.local.entity.TestResultEntity
import kotlinx.coroutines.flow.Flow

class TestResultRepository(private val resultDao: TestResultDao) {

    val allResults: Flow<List<TestResultEntity>> = resultDao.getAllResults()

    fun getResultsBySession(sessionId: Long): Flow<List<TestResultEntity>> =
        resultDao.getResultsBySession(sessionId)

    fun getRecentResultsByRouter(routerId: Long, limit: Int = 50): Flow<List<TestResultEntity>> =
        resultDao.getRecentResultsByRouter(routerId, limit)

    suspend fun insertResult(result: TestResultEntity) = resultDao.insertResult(result)

    suspend fun insertResults(results: List<TestResultEntity>) = resultDao.insertResults(results)

    suspend fun deleteResult(result: TestResultEntity) = resultDao.deleteResult(result)

    suspend fun deleteAll() = resultDao.deleteAll()
}