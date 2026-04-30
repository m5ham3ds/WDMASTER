package com.wdmaster.app.presentation.settings

import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.repository.SettingsRepository
import com.wdmaster.app.domain.usecase.ManageRoutersUseCase
import com.wdmaster.app.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RouterManagerViewModel(
    private val manageRoutersUseCase: ManageRoutersUseCase,
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    val routers: StateFlow<List<RouterProfileEntity>> = manageRoutersUseCase.allRouters
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
        data class NavigateToTest(val routerId: Long) : UiEvent()
    }
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun addRouter(router: RouterProfileEntity) {
        viewModelScope.launch {
            manageRoutersUseCase(ManageRoutersUseCase.Action.Add(router))
        }
    }

    fun updateRouter(router: RouterProfileEntity) {
        viewModelScope.launch {
            manageRoutersUseCase(ManageRoutersUseCase.Action.Update(router))
        }
    }

    fun deleteRouter(router: RouterProfileEntity) {
        viewModelScope.launch {
            manageRoutersUseCase(ManageRoutersUseCase.Action.Delete(router))
        }
    }

    fun testRouter(router: RouterProfileEntity) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateToTest(router.id))
        }
    }
}