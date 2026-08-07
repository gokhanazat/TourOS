package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2CLiveLocationItem
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.ObserveB2CLiveLocationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2CLiveLocationUiState(
    val liveLocation: B2CLiveLocationItem = B2CLiveLocationItem(),
    val isRealtimeConnected: Boolean = true,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class B2CLiveLocationViewModel(
    private val observeB2CLiveLocationUseCase: ObserveB2CLiveLocationUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2CLiveLocationUiState())
    val uiState: StateFlow<B2CLiveLocationUiState> = _uiState.asStateFlow()

    init {
        startLiveTracking()
    }

    fun startLiveTracking(tourId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            observeB2CLiveLocationUseCase(tenantId, tourId).collect { location ->
                _uiState.value = _uiState.value.copy(
                    liveLocation = location,
                    isLoading = false,
                    isRealtimeConnected = true
                )
            }
        }
    }
}
