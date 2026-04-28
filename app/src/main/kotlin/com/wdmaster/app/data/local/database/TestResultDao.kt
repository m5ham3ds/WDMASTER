package com.wdmaster.app.data.local.database

import androidx.room.*
import com.wdmaster.app.data.local.entity.TestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {

    @Query("SELECT * FROM test_results ORDER BY tested_at DESC")
    fun getAllResults(): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE session_id = :sessionId")
    fun getResultsBySession(sessionId: Long): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE router_id = :routerId ORDER BY tested_at DESC LIMIT :limit")
    fun getRecentResultsByRouter(routerId: Long, limit: Int = 50): Flow<List<TestResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<TestResultEntity>)

    @Delete
    suspend fun deleteResult(result: TestResultEntity)

    @Query("DELETE FROM test_results")
    suspend fun deleteAll()
}