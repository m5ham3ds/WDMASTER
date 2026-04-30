package com.wdmaster.app.data.local.database

import androidx.room.*
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouterProfileDao {

    @Query("SELECT * FROM router_profiles ORDER BY is_default DESC, created_at DESC")
    fun getAllRouters(): Flow<List<RouterProfileEntity>>

    @Query("SELECT * FROM router_profiles WHERE id = :id")
    suspend fun getRouterById(id: Long): RouterProfileEntity?

    @Query("SELECT * FROM router_profiles WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultRouter(): RouterProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouter(router: RouterProfileEntity)

    @Update
    suspend fun updateRouter(router: RouterProfileEntity)

    @Delete
    suspend fun deleteRouter(router: RouterProfileEntity)

    @Query("UPDATE router_profiles SET is_default = 0 WHERE is_default = 1")
    suspend fun clearDefault()

    @Query("UPDATE router_profiles SET is_default = 1 WHERE id = :id")
    suspend fun setDefault(id: Long)
}