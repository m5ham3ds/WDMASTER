package com.wdmaster.app.data.repository

import com.wdmaster.app.data.local.database.SessionDao
import com.wdmaster.app.data.local.entity.TestSessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {

    val allSessions: Flow<List<TestSessionEntity>> = sessionDao.getAllSessions()

    suspend fun getSessionById(id: Long): TestSessionEntity? = sessionDao.getSessionById(id)

    suspend fun getRunningSession(): TestSessionEntity? = sessionDao.getRunningSession()

    suspend fun insertSession(session: TestSessionEntity): Long =
        sessionDao.insertSession(session)

    suspend fun updateSession(session: TestSessionEntity) =
        sessionDao.updateSession(session)

    suspend fun finishSession(id: Long) = sessionDao.finishSession(id)

    suspend fun deleteSession(session: TestSessionEntity) =
        sessionDao.deleteSession(session)

    suspend fun deleteAll() = sessionDao.deleteAll()
}