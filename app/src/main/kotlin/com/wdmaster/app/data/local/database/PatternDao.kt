package com.wdmaster.app.data.local.database

import androidx.room.*
import com.wdmaster.app.data.local.entity.SuccessfulPatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {

    @Query("SELECT * FROM successful_patterns ORDER BY discovered_at DESC")
    fun getAllPatterns(): Flow<List<SuccessfulPatternEntity>>

    @Query("SELECT * FROM successful_patterns WHERE router_id = :routerId")
    fun getPatternsByRouter(routerId: Long): Flow<List<SuccessfulPatternEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: SuccessfulPatternEntity)

    @Delete
    suspend fun deletePattern(pattern: SuccessfulPatternEntity)

    @Query("DELETE FROM successful_patterns")
    suspend fun deleteAll()
}