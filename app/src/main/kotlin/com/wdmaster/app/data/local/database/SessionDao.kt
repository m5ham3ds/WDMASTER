package com.wdmaster.app.data.local.database

import androidx.room.*
import com.wdmaster.app.data.local.entity.TestSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM test_sessions ORDER BY started_at DESC")
    fun getAllSessions(): Flow<List<TestSessionEntity>>

    @Query("SELECT * FROM test_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): TestSessionEntity?

    @Query("SELECT * FROM test_sessions WHERE is_running = 1 LIMIT 1")
    suspend fun getRunningSession(): TestSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TestSessionEntity): Long

    @Update
    suspend fun updateSession(session: TestSessionEntity)

    @Query("UPDATE test_sessions SET is_running = 0, finished_at = :finishedAt WHERE id = :id")
    suspend fun finishSession(id: Long, finishedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteSession(session: TestSessionEntity)

    @Query("DELETE FROM test_sessions")
    suspend fun deleteAll()
}