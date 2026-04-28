package com.wdmaster.app.domain.usecase

import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.repository.RouterRepository
import kotlinx.coroutines.flow.Flow

class ManageRoutersUseCase(private val routerRepository: RouterRepository) {

    val allRouters: Flow<List<RouterProfileEntity>> = routerRepository.allRouters

    suspend operator fun invoke(action: Action) {
        when (action) {
            is Action.Add -> routerRepository.insertRouter(action.router)
            is Action.Update -> routerRepository.updateRouter(action.router)
            is Action.Delete -> routerRepository.deleteRouter(action.router)
            is Action.SetDefault -> routerRepository.setDefaultRouter(action.id)
        }
    }

    suspend fun getRouterById(id: Long): RouterProfileEntity? =
        routerRepository.getRouterById(id)

    suspend fun getDefaultRouter(): RouterProfileEntity? =
        routerRepository.getDefaultRouter()

    sealed class Action {
        data class Add(val router: RouterProfileEntity) : Action()
        data class Update(val router: RouterProfileEntity) : Action()
        data class Delete(val router: RouterProfileEntity) : Action()
        data class SetDefault(val id: Long) : Action()
    }
}