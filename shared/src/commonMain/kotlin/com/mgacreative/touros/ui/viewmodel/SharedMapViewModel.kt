package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.SharedMapPoint
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetSharedMapPointsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SharedMapUiState(
    val selectedLayer: String = "ALL", // HOTELS, ROUTES, LIVE_VEHICLE, ALL
    val mapPoints: List<SharedMapPoint> = emptyList(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class SharedMapViewModel(
    private val getSharedMapPointsUseCase: GetSharedMapPointsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedMapUiState())
    val uiState: StateFlow<SharedMapUiState> = _uiState.asStateFlow()

    init {
        loadMapPoints()
    }

    fun selectLayer(layer: String) {
        _uiState.value = _uiState.value.copy(selectedLayer = layer)
        loadMapPoints()
    }

    fun loadMapPoints() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getSharedMapPointsUseCase(_uiState.value.selectedLayer, tenantId)
            res.onSuccess { points ->
                _uiState.value = _uiState.value.copy(
                    mapPoints = points,
                    isLoading = false,
                    notificationMessage = "🗺️ Ortak Harita Bileşeni (Expect/Actual) ${points.size} İşaretçi İle Güncellendi!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message
                )
            }
        }
    }
}
