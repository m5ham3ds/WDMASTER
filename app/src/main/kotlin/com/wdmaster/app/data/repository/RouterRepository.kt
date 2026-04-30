package com.wdmaster.app.data.repository

import com.wdmaster.app.data.local.database.RouterProfileDao
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import kotlinx.coroutines.flow.Flow

class RouterRepository(private val routerDao: RouterProfileDao) {

    val allRouters: Flow<List<RouterProfileEntity>> = routerDao.getAllRouters()

    suspend fun getRouterById(id: Long): RouterProfileEntity? = routerDao.getRouterById(id)

    suspend fun getDefaultRouter(): RouterProfileEntity? = routerDao.getDefaultRouter()

    suspend fun insertRouter(router: RouterProfileEntity) = routerDao.insertRouter(router)

    suspend fun updateRouter(router: RouterProfileEntity) = routerDao.updateRouter(router)

    suspend fun deleteRouter(router: RouterProfileEntity) = routerDao.deleteRouter(router)

    suspend fun setDefaultRouter(id: Long) {
        routerDao.clearDefault()
        routerDao.setDefault(id)
    }
}